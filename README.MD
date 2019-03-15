java -Dmode=server -Dport=9991 -jar test-0.0.1-SNAPSHOT.jar
java -Dmode=client -Dhost=172.30.251.33 -Dport=9991 -Dtps=8 -Dspring.profiles.active=test -jar test-0.0.1-SNAPSHOT.jar