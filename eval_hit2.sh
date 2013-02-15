#!/bin/sh
# Overall data
# Model 1
./eval_codeswitch.py -a plain data/cs_hit2_clean.txt 1.0 > /dev/null 2> perf/perf_hit2_a_1.0.txt &
./eval_codeswitch.py -ae plain data/cs_hit2_clean.txt 1.0 > /dev/null 2> perf/perf_hit2_e_1.0.txt &

# Model 1.5, all
./eval_codeswitch.py -a plain data/cs_hit2_clean.txt 1.5 unk left > /dev/null 2> perf/perf_hit2_a_1.5_unk_left.txt &
./eval_codeswitch.py -a plain data/cs_hit2_clean.txt 1.5 unk right > /dev/null 2> perf/perf_hit2_a_1.5_unk_right.txt &
./eval_codeswitch.py -a plain data/cs_hit2_clean.txt 1.5 unk random > /dev/null 2> perf/perf_hit2_a_1.5_unk_random.txt &

# Model 1.5, no entities
./eval_codeswitch.py -ae plain data/cs_hit2_clean.txt 1.5 unk left > /dev/null 2> perf/perf_hit2_e_1.5_unk_left.txt &
./eval_codeswitch.py -ae plain data/cs_hit2_clean.txt 1.5 unk right > /dev/null 2> perf/perf_hit2_e_1.5_unk_right.txt &
./eval_codeswitch.py -ae plain data/cs_hit2_clean.txt 1.5 unk random > /dev/null 2> perf/perf_hit2_e_1.5_unk_random.txt

# Codeswitched only
# Model 1
./eval_codeswitch.py -a plain data/cs_hit2_clean_cs.txt 1.0 > /dev/null 2> perf/perf_hit2cs_a_1.0.txt &
./eval_codeswitch.py -ae plain data/cs_hit2_clean_cs.txt 1.0 > /dev/null 2> perf/perf_hit2cs_e_1.0.txt &

# Model 1.5, all
./eval_codeswitch.py -a plain data/cs_hit2_clean_cs.txt 1.5 unk left > /dev/null 2> perf/perf_hit2cs_a_1.5_unk_left.txt &
./eval_codeswitch.py -a plain data/cs_hit2_clean_cs.txt 1.5 unk right > /dev/null 2> perf/perf_hit2cs_a_1.5_unk_right.txt &
./eval_codeswitch.py -a plain data/cs_hit2_clean_cs.txt 1.5 unk random > /dev/null 2> perf/perf_hit2cs_a_1.5_unk_random.txt &

# Model 1.5, no entities
./eval_codeswitch.py -ae plain data/cs_hit2_clean_cs.txt 1.5 unk left > /dev/null 2> perf/perf_hit2cs_e_1.5_unk_left.txt &
./eval_codeswitch.py -ae plain data/cs_hit2_clean_cs.txt 1.5 unk right > /dev/null 2> perf/perf_hit2cs_e_1.5_unk_right.txt &
./eval_codeswitch.py -ae plain data/cs_hit2_clean_cs.txt 1.5 unk random > /dev/null 2> perf/perf_hit2cs_e_1.5_unk_random.txt
