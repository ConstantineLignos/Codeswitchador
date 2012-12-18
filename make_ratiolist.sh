SMOOTH=$1
CORPUS1=$2
CORPUS2=$3
OUTPUT=$4

echo Running ratio list script...
echo Job beginning at `date`
python freqratio.py -j $SMOOTH $CORPUS1 $CORPUS2 > $OUTPUT
echo Job ending at `date`
