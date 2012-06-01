#this script will be execute after the crawl is finished. with one parameter. this parameter is the crawldirectory of the crawl. you can get this parameter with $1
# for example: create a file after.txt in the specified crawldir: touch $1/after.txt

# remove orphaned hadoop temp files
find "hadoop-ingrid" -name "job_local*" -mtime +5 -exec rm -r {} \;
find "hadoop-ingrid" -name "*-temp-*" -mtime +5 -exec rm -r {} \;
