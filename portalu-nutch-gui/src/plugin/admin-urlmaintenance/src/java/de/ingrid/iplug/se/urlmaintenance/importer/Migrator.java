package de.ingrid.iplug.se.urlmaintenance.importer;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.ingrid.iplug.se.urlmaintenance.parse.CsvParser;
import de.ingrid.iplug.se.urlmaintenance.parse.IUrlFileParser;
import de.ingrid.iplug.se.urlmaintenance.parse.UrlContainer;
import de.ingrid.iplug.se.urlmaintenance.parse.UrlContainer.UrlType;
import de.ingrid.iplug.se.urlmaintenance.persistence.dao.CatalogUrlDao;
import de.ingrid.iplug.se.urlmaintenance.persistence.dao.ExcludeUrlDao;
import de.ingrid.iplug.se.urlmaintenance.persistence.dao.ICatalogUrlDao;
import de.ingrid.iplug.se.urlmaintenance.persistence.dao.IExcludeUrlDao;
import de.ingrid.iplug.se.urlmaintenance.persistence.dao.ILimitUrlDao;
import de.ingrid.iplug.se.urlmaintenance.persistence.dao.IMetadataDao;
import de.ingrid.iplug.se.urlmaintenance.persistence.dao.IProviderDao;
import de.ingrid.iplug.se.urlmaintenance.persistence.dao.IStartUrlDao;
import de.ingrid.iplug.se.urlmaintenance.persistence.dao.LimitUrlDao;
import de.ingrid.iplug.se.urlmaintenance.persistence.dao.MetadataDao;
import de.ingrid.iplug.se.urlmaintenance.persistence.dao.ProviderDao;
import de.ingrid.iplug.se.urlmaintenance.persistence.dao.StartUrlDao;
import de.ingrid.iplug.se.urlmaintenance.persistence.model.Provider;
import de.ingrid.iplug.se.urlmaintenance.persistence.service.TransactionService;

public class Migrator {
    
    private ContainerCommand webContainerCommand;
    private ContainerCommand catalogContainerCommand;
    
    private IProviderDao     providerDao;
    private IStartUrlDao     startUrlDao;
    private ILimitUrlDao     limitUrlDao;
    private IExcludeUrlDao   excludeUrlDao;
    private ICatalogUrlDao   catalogUrlDao;
    private IMetadataDao     metadataDao;
    
    private Map<String, Long>   providerMap             = new HashMap<String, Long>();
    private List<String>        notImportedFiles        = new ArrayList<String>();
    private List<String>        successfulImportedFiles = new ArrayList<String>();
    private List<String>        errorImportedFiles      = new ArrayList<String>();
    
    private TransactionService  tService;
    
    private WebUrlValidator     webUrlValidator;
    private CatalogUrlValidator catalogUrlValidator;
    
    
    public Migrator() {
        webContainerCommand = new ContainerCommand();
        webContainerCommand.setType(UrlContainer.UrlType.WEB);
        
        catalogContainerCommand = new ContainerCommand();
        catalogContainerCommand.setType(UrlContainer.UrlType.CATALOG);
        
        // initialize all DAOs
        tService = new TransactionService();
        providerDao   = new ProviderDao(tService);
        startUrlDao   = new StartUrlDao(tService);
        limitUrlDao   = new LimitUrlDao(tService);
        excludeUrlDao = new ExcludeUrlDao(tService);
        catalogUrlDao = new CatalogUrlDao(tService);
        metadataDao   = new MetadataDao(tService);
        
        // initialize url validators
        webUrlValidator     = new WebUrlValidator(providerDao, startUrlDao);
        catalogUrlValidator = new CatalogUrlValidator(providerDao, catalogUrlDao);
        
        tService.beginTransaction();
        List<Provider> provider = providerDao.getAll();
        for (Provider p : provider) {
            providerMap.put(p.getShortName(), p.getId());
        }
        tService.commitTransaction();
    }

    private void importUrls(File exportDirectory, UrlType urlType) {
        if (urlType == UrlContainer.UrlType.WEB)
            findFilesAndImport(new File(exportDirectory, "web"), urlType);
        else
            findFilesAndImport(new File(exportDirectory, "catalog"), urlType);
            
        
    }
    
    private void findFilesAndImport(File exportDirectory, UrlType urlType) {
        if (exportDirectory.isDirectory()) {
            // get all files ending with "csv" and directories
            String filenames[] = exportDirectory.list(new FilenameFilter() { 
                @Override public boolean accept( File f, String s) {
                    return new File(f,s).isDirectory() || s.toLowerCase().endsWith( ".csv" ); 
                }});
            
            for (String filename : filenames) {
                File subFile = new File(exportDirectory, filename);
                if (subFile.isDirectory())
                    findFilesAndImport(subFile, urlType);
                else {
                    importFile(subFile, urlType);
                }
            }
            System.out.println("Import-Status: " + (webContainerCommand.getContainers().size()+
                    catalogContainerCommand.getContainers().size()) + " URLs imported");
        }
    }

    private void importFile(File filename, UrlType urlType) {
        Long providerId = providerMap.get(filename.getName().substring(0, filename.getName().length()-4));
        if (providerId == null) {
            notImportedFiles.add(filename.getName());
            return;
        }
        
        final IUrlFileParser parser = new CsvParser(urlType, providerId);
        boolean error = false;
        try {
            parser.parse(filename);
        
            while (parser.hasNext()) {
                final UrlContainer container = parser.next();
                final HashMap<String, String> errorCodes = new HashMap<String, String>();
                if (urlType == UrlContainer.UrlType.WEB) {
                    if (!webUrlValidator.validate(container, errorCodes))
                        error = true;
                    webContainerCommand.addContainer(container, errorCodes);
                } else {
                    if (!catalogUrlValidator.validate(container, errorCodes))
                        error = true;
                    catalogContainerCommand.addContainer(container, errorCodes);
                }
            }
            
            if (error)
                errorImportedFiles.add(filename.getName());
            else
                successfulImportedFiles.add(filename.getName());
        } catch (Exception e) {
            errorImportedFiles.add(filename.getName());
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    private void storeUrlsIntoDB() {
        System.out.println("\nStoring imported URLs into DB ...");

        tService.beginTransaction();
        int nr = 0;
        UrlsController urlController = new UrlsController(startUrlDao, limitUrlDao, excludeUrlDao, catalogUrlDao, providerDao, metadataDao);
        
        // iterate over all imported URL container and store them into the DB
        Map<UrlContainer, Map<String, String>> containers = new HashMap<UrlContainer, Map<String, String>>();
        
        // merge web and catalog containers, they will be identified through the urlType
        containers.putAll(webContainerCommand.getContainers());
        containers.putAll(catalogContainerCommand.getContainers());
        for (UrlContainer container : containers.keySet()) {
            // skip if this url had an validation error
            if (containers.get(container).size() > 0) 
                continue;
            
            if (nr++ % 100 == 0)
                System.out.println("Already stored: " + nr);
            
            // store URL
            urlController.saveContainer(container);
        }
        
        // show URLs that couldn't be imported due to an error
        List<String> errors = urlController.errorURLList();
        if (errors.size() > 0) {
            System.out.println("From "+nr+" URLs the following ones couldn't be stored to the DB:");
            for (String error : errors) {
                System.out.print(error + ", ");
            }
        } else
            System.out.println("No problems during storing "+nr+" URLs into DB.");
        
        tService.commitTransaction();
        tService.close();
    }
    
    public void printSummary() {
        System.out.println();
        System.out.println("SUMMARY");
        System.out.println("-------");
        System.out.println();
        System.out.println("Successful:");
        for (String file: successfulImportedFiles) {
            System.out.print(file + ", ");
        }
        System.out.println();
        System.out.println();
        
        System.out.println("with error:");
        for (String file: errorImportedFiles) {
            System.out.print(file + ", ");
        }
        System.out.println();
        System.out.println();
        
        System.out.println("no matching provider:");
        for (String file: notImportedFiles) {
            System.out.print(file + ", ");
        }
        System.out.println();
        System.out.println();
        
        System.out.println("detailed web-url errors:");
        Map<UrlContainer, Map<String, String>> containers = webContainerCommand.getContainers();
        for (UrlContainer container: containers.keySet()) {
            // if this URL had an validation error
            if (containers.get(container).size() > 0) {
                System.out.println(container.getStartUrl().getUrl());
                Map<String, String> errorMap = containers.get(container);
                for (String errorCode : errorMap.keySet()) {
                    System.out.print("\t" + errorCode + ": " + errorMap.get(errorCode) + "\n");    
                }
            }
        }
        
        System.out.println("\ndetailed catalog-url errors:");
        containers = catalogContainerCommand.getContainers();
        for (UrlContainer container: containers.keySet()) {
            // if this URL had an validation error
            if (containers.get(container).size() > 0) {
                System.out.println(container.getMetadatas().keySet());
                Map<String, String> errorMap = containers.get(container);
                for (String errorCode : errorMap.keySet()) {
                    System.out.print("\t" + errorCode + ": " + errorMap.get(errorCode) + "\n");    
                }
            }
        }
        System.out.println();
    }
    
    
    /**
     * @param args
     */
    public static void main(String[] args) {
        boolean dryRun = false;
        
        // check preconditions
        if (args.length < 1 || args.length > 2) {
            System.out.println("Usage:\n\t Migrator <Path-to-export-directory> [-test]");
            System.out.println("\tThe optional parameter '-test' does a dry-run without " +
            		"storing any URL into the database. This is useful to check the " +
            		"exported data for errors."); 
            return;
        }

        File exportDirectory = new File(args[0]);
        
        if (args.length == 2) {
            if ("-test".equals(args[1]))
                dryRun = true;
            else {
                System.out.println("Do not understand parameter: " + args[1] + "!\nParameter should be '-test'");
                return;
            }
        }
        
        if (checkDirectory(exportDirectory) == false) {
            System.out.println("Error: Export directory doesn't contain web and/or catalog directory!");
            return;
        }
        
        
        // start the migration process
        Migrator migrator = new Migrator();
        
        // import WEB-URLs
        migrator.importUrls(exportDirectory, UrlContainer.UrlType.WEB);
        
        // import CATALOG-URLs
        migrator.importUrls(exportDirectory, UrlContainer.UrlType.CATALOG);
        
        // save URLs into DB if this is not a dry-run
        if (!dryRun)
            migrator.storeUrlsIntoDB();
        
        // print a summary showing errors and statistics
        migrator.printSummary();
    }

    /**
     * Check for a sub-directory called web and/or catalog which should
     * exist after an export was done.
     * 
     * @param exportDirectory, the directory to the exported directory
     * @return true if found, otherwise false
     */
    private static boolean checkDirectory(File exportDirectory) {
        String[] subFiles = exportDirectory.list();
        
        for (String file : subFiles) {
            if (file.equals("web") || file.equals("catalog"))
                return true;
        }
        
        return false;
    }

}
