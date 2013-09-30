web: target/start -Dhttp.port=${PORT} -javaagent:newrelic/newrelic.jar
pdfworker: java ${JAVA_OPTS} -cp "target/staged/*" com.appirio.workers.pdf.WorkerProcess -javaagent:newrelic/newrelic.jar