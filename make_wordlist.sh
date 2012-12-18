CORPUS=$1
OUTPUT=$2

echo Running wordlist script...
echo Job beginning at `date`
python wordlist.py -j $CORPUS > $OUTPUT
echo Job ending at `date`
