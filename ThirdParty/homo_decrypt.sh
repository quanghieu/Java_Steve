#!/bin/bash

encrypt_loc=$1
inputFile=$2
keyfile=$3
outputFile=$4

cd $encrypt_loc
echo "cd $encrypt_loc" 
pwd
ls
echo "./decrypt $inputFile $keyfile $outputFile"

./decrypt $inputFile $keyfile $outputFile

perl piwigo_upload.pl --url=http://localhost/piwigo --user=hieua81992 --password=23081992 --file=$outputFile --album_id=435
echo "perl piwigo_upload.pl --url=http://localhost/piwigo --user=hieua81992 --password=23081992 --file=$outputFile --album_id=435"

