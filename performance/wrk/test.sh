# https://github.com/wg/wrk
wrk -d 60 -c 256 -t 40 --latency -s script.lua http://localhost:8080/ &> "testResult$(date +%s).txt"
# -duration : 60 second -c 256 concurent connections -t 40 threads