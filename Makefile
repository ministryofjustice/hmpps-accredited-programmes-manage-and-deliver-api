SHELL     := /bin/bash
JAVA_OPTS := "-Xmx4096m -XX:ParallelGCThreads=2 -XX:ConcGCThreads=2 -Djava.util.concurrent.ForkJoinPool.common.parallelism=2 -Dorg.gradle.daemon=true -Dkotlin.compiler.execution.strategy=in-process -Dorg.gradle.workers.max=1"


start-containers:
ifeq (0,$(shell docker compose ps --services --filter "status=running" | grep 'hmpps-accredited-programmes-manage-and-deliver-api' | wc -l | xargs))
	docker compose up -d
else
	@echo "containers already running"
endif
