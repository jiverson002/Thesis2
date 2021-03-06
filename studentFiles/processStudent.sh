directories=( mine mine_noSort mine_noID mine_nothing original mine_noID_moss mine_moss )
RUNTOOLS=/home/ooee/Thesis2/otherTools/runTools.sh
NORMALIZE=/home/ooee/Thesis2/studentFiles/normalizeScript.sh

$NORMALIZE
echo "Begin running tools"
for D in ${directories[@]}
do
   cd $D
   $RUNTOOLS
   cd ..
done
echo "Starting Moss runs"
cd mine_moss
/home/ooee/Thesis2/otherTools/Moss/moss.sh *.c > Report
cd ../mine_noID_moss
/home/ooee/Thesis2/otherTools/Moss/moss.sh *.c > Report
cd ../mine_nothing_moss
/home/ooee/Thesis2/otherTools/Moss/moss.sh *.c > Report
echo "Finish processStudent.sh"
