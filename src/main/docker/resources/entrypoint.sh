#!/bin/sh
echo "I LOVE MICROSERVICE"
if [ -n "$MEMORY_LIMIT" ];
 then
  value=$(numfmt  --from=auto  --grouping $MEMORY_LIMIT)
  value=$(($value/1048576)) # convert to MB
  echo "MEMORY_LIMIT: ${value}MB"
  memory_left=$(awk "BEGIN { memory = int($value * 0.1); if (memory <50) {memory = 50} print memory} ")
  echo "${memory_left}MB is left for system"
  value=$(awk "BEGIN { print(int($value - $memory_left))}") # leave memory space for system
  echo "${value}MB is left for application"
  if [ $value -lt "128" ]; # if less then 128MB fail
  then
    echo "Memory left for application is to small must be at lest 128MB"
    exit 1;
   else
    metaspace=$(awk "BEGIN { memory= int($value * 0.1); if (memory >1024) {memory = 1024} else if ( memory < 64 ){ memory = 64 } print memory} ") # take 10% of available memory to metaspace
    heap=$(($value - $metaspace))
  fi

  jvm_heap=""
  jvm_metaspace=""
  jvm_variable_heap="-Xmx${heap}m"

  echo "Using JDK8+ memory settings"
  jvm_variable_metaspace="-XX:MaxMetaspaceSize=${metaspace}m"

  export JAVA_MEM="${jvm_heap:-`echo $jvm_variable_heap`} ${jvm_metaspace:-`echo $jvm_variable_metaspace`}"
  echo "Java Memory Settings: $JAVA_MEM, memory limit: $MEMORY_LIMIT"
fi

jvm_gc=${JAVA_GC:-"-XX:+UseG1GC -XX:+UseStringDeduplication -XX:MinHeapFreeRatio=25 -XX:MaxHeapFreeRatio=75"}
jvm_mem=${JAVA_MEM:-" "}
jvm_opts=${JAVA_OPTS:-"-server -XX:HeapDumpPath=/var/log/c8yexample/heap-dump-%p.hprof"}
arguments=${ARGUMENTS:-" --package.name=hello --package.directory=c8yexample"}

proxy_params=""
if [ -n "$PROXY_HTTP_HOST" ]; then proxy_params="-Dhttp.proxyHost=${PROXY_HTTP_HOST} -DproxyHost=${PROXY_HTTP_HOST}"; fi
if [ -n "$PROXY_HTTP_PORT" ]; then proxy_params="${proxy_params} -Dhttp.proxyPort=${PROXY_HTTP_PORT} -DproxyPort=${PROXY_HTTP_PORT}"; fi
if [ -n "$PROXY_HTTP_NON_PROXY_HOSTS" ]; then proxy_params="${proxy_params} -Dhttp.nonProxyHosts=\"${PROXY_HTTP_NON_PROXY_HOSTS}\""; fi
if [ -n "$PROXY_HTTPS_HOST" ]; then proxy_params="${proxy_params} -Dhttps.proxyHost=${PROXY_HTTPS_HOST}"; fi
if [ -n "$PROXY_HTTPS_PORT" ]; then proxy_params="${proxy_params} -Dhttps.proxyPort=${PROXY_HTTPS_PORT}"; fi
if [ -n "$PROXY_SOCKS_HOST" ]; then proxy_params="${proxy_params} -DsocksProxyHost=${PROXY_SOCKS_HOST}"; fi
if [ -n "$PROXY_SOCKS_PORT" ]; then proxy_params="${proxy_params} -DsocksProxyPort=${PROXY_SOCKS_PORT}"; fi


mkdir -p /var/log/hello; echo "heap dumps  /var/log/hello/heap-dump-<pid>.hprof"

java ${jvm_opts} ${jvm_gc} ${jvm_mem} ${proxy_params} -jar /data/hello.jar ${arguments}
