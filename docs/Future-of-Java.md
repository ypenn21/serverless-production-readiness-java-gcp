# The Future of Java Performance in Cloud Run: Native Java, CRaC and Project Leyden

Applications run better on Cloud Run if they start fast, with instant peak performance and lower CPU/memory consumption. 
You’ll save on costs if the application needs less time and resources to run on, handle more requests with less CPU/memory and 
achieve better performance.

We love to use Java for its stability, performance and portability, however we all know that Java and its various web frameworks aren't known 
for starting fast or not using a lot of resources.

Don’t worry, I’ll start exploring carefully how this all changes with three technologies geared towards improving Java app runtime efficiency in serverless environments.
* Native Java Images, with [GraalVM](https://www.graalvm.org/)
* JVM Checkpoint and Restore, with [CRaC](https://wiki.openjdk.org/display/crac/Main)
* Upcoming OpenJDK runtime efficiency project, [Project Leyden](https://openjdk.org/projects/leyden/)

This is a (fast) emerging space where nothing is completely done at this time, with multiple paths to optimizing Java runtime efficiency for 
your Cloud Run applications shaping up. Java performance optimization in Cloud Run is a sum of multiple factors: the cloud runtime, 
the JVM, the web frameworks/dependencies and, last but not least, your application code.

In this blog post we’ll dive into these optimization options, starting from the current state. 
My goal is to help you identify the best options for __*your*__ *application running in production in Cloud Run*.

## Sample apps
The blog post is supported by  Spring Boot sample apps, using the latest [Java 21](https://openjdk.org/projects/jdk/21/) LTS,
[Spring Boot](https://github.com/spring-projects/spring-boot) and [Spring Framework](https://github.com/spring-projects/spring-framework/wiki/What's-New-in-Spring-Framework-6.x) versions, including build, test, deployment and runtime guidelines to [Google Cloud Run](https://cloud.google.com/run/?utm_source=google&utm_medium=cpc&utm_campaign=na-none-all-en-dr-sitelink-all-all-trial-e-gcp-1605212&utm_content=text-ad-none-any-DEV_c-CRE_665735485586-ADGP_Hybrid+%7C+BKWS+-+MIX+%7C+Txt_Cloud+Run-KWID_43700077225654501-kwd-678836618089-userloc_9000912&utm_term=KW_google%20cloud%20run-ST_google+cloud+run-NET_g-&gclid=CjwKCAjwv-2pBhB-EiwAtsQZFB8K6FUxSvkHEktLRF1UpODa1du2ZawyO82eHqP_CvKJO_jABnb8_hoC5UwQAvD_BwE&gclsrc=aw.ds).

Start by cloning the [Git repository supporting this blog](https://github.com/GoogleCloudPlatform/serverless-production-readiness-java-gcp/tree/main) 
and [setting up your environment](https://github.com/GoogleCloudPlatform/serverless-production-readiness-java-gcp/tree/main?tab=readme-ov-file#getting-started). 
Follow along the blog with instructions on building the Quotes service as a [JIT image](https://github.com/GoogleCloudPlatform/serverless-production-readiness-java-gcp/blob/main/services/quotes/README.md), 
[GraalVM Native image](https://github.com/GoogleCloudPlatform/serverless-production-readiness-java-gcp/blob/main/services/quotes/README.md), 
respectively [CRaC image](https://github.com/GoogleCloudPlatform/serverless-production-readiness-java-gcp/blob/main/runtimes/crac/quotes-crac/README.md) and deploying them to Cloud Run.

A super-early [Project Leyden](https://github.com/GoogleCloudPlatform/serverless-production-readiness-java-gcp/blob/main/runtimes/project-leyden/README.md) sample has also been provided.

## Key terms
Here are a few concepts that I’ll be referring to:
* Startup latency: time to get to processing the first request in the app
* Warmup: time it takes the app to reach peak performance
* Peak performance: level of performance where the Java app handles the highest possible workload with the lowest possible latency and resource consumption
* RSS memory: amount of physical memory actively used by a Java process

## __Plain JIT Images (OpenJDK)__
In traditional Java applications, source code is compiled into bytecode and packaged into a Jar archive (and containerized). 
The JVM uses a bytecode interpreter to execute the program on the host runtime. The [JIT compiler](https://docs.oracle.com/en/database/oracle/oracle-database/21/jjdev/Oracle-JVM-JIT.html#GUID-23D5BA60-A2B3-45F9-93DF-81A3D971CA50) translates Java bytecode frequently executed code (hotspots) into machine code to improve peak performance.

It is important for us to understand this process, with JIT being the default compiler in the JVM. We want to use it as a benchmark against which to measure improvements.

__Fast build time + less initial optimization → slower startup + higher resource consumption__

![Image1.png](images/Image1.png)

Note the start-up time of the [Quotes service](https://github.com/GoogleCloudPlatform/serverless-production-readiness-java-gcp/blob/main/services/quotes/README.md) JIT container image:
```shell
> docker run --rm -p8080:8083 quotes
Starting QuotesApplication v1.0.0 using Java 21 with PID 1
...
Tomcat started on port 8083 (http) with context path ''
Started QuotesApplication in 3.224 seconds (process running for 3.466)
```
While ~3 seconds might sound acceptable to many applications, in the real world, when you run your enterprise-grade apps in the cloud, this startup time might be measured in tens of seconds or even minutes. Scale-to-zero would potentially be unacceptable, while scale-out speed severely impacted.

__JIT Images at runtime in Cloud Run__
[CPU Boost](https://cloud.google.com/blog/products/serverless/announcing-startup-cpu-boost-for-cloud-run--cloud-functions) in Cloud Run 
offers a great feature for improving the cold startup time for JIT images, by dynamically allocating more CPU to your container during startup, 
with some applications observing a 50% reduction in startup time. It could be sufficient to meet the SLOs for your application.

## __Next-level AOT performance with Native Java Images (GraalVM)__
Native Images follow the same process as above, however the application is transformed ahead-of-time (AOT) into a native executable at build time, for the individual OS and Machine Architecture of the runtime environment, and can run without the need of a full JVM.

Building Native Images leveraging AOT compilation makes [closed-world assumptions](https://docs.oracle.com/en/graalvm/enterprise/21/docs/reference-manual/native-image/basics/#static-analysis) (at build time) of all the classes required by the 
application (at runtime). This static analysis is time consuming. The resulting application image contains only the classes required 
to run the app, with no further JIT runtime optimizations. This results in smaller container images, for faster and more efficient deployment, 
while limiting the security attack surface.

The static analysis commences from your application entry point and includes any class which can be reached across source code, 
dependent libraries, respectively JDK classes. If classes can’t be reached due to Java’s dynamic features, say reflection, proxying, 
serialization, resource access, they will not be included in the image and would have to be supplied externally through configuration, 
called [shared metadata](https://medium.com/graalvm/enhancing-3rd-party-library-support-in-graalvm-native-image-with-shared-metadata-9eeae1651da4).

__Slow build time + higher optimization → super-fast startup + lower resource consumption__

![Image2.png](images/Image2.png)

__Why use native images with GraalVM?__

![Image3.png](images/Image3.png)

__Any GraalVM trade-offs?__

![Image4.png](images/Image4.png)

__Warmup for peak performance with GraalVM__

Let’s note that peak performance was an additional trade-off for GraalVM usage, as there was no just-in-time optimization at runtime.

The recent release of the [Oracle GraalVM](https://www.graalvm.org/downloads/) distribution under the GraalVM Free Terms and Conditions [license](https://medium.com/graalvm/a-new-graalvm-release-and-new-free-license-4aab483692f5) 
(see [restrictions](https://www.oracle.com/java/technologies/javase/jdk-faqs.html#GraalVM-licensing)) promises to address this aspect with the 
introduction of [profile-guided-optimization(PGO)](https://www.graalvm.org/latest/reference-manual/native-image/guides/optimize-native-executable-with-pgo/) 
for peak performance and the availability of [G1GC](https://www.graalvm.org/latest/reference-manual/native-image/optimizations-and-performance/MemoryManagement/#g1-garbage-collector) in GraalVM.

__Note__ the startup time of the [Quotes service](https://github.com/GoogleCloudPlatform/serverless-production-readiness-java-gcp/blob/main/services/quotes/README.md) Native Java container image:
```shell
> docker run --rm -p8080:8083 quotes-native
Starting AOT-processed QuotesApplication using Java 21 with PID 1
...
Tomcat started on port 8083 (http) with context path ''
Started QuotesApplication in 0.392 seconds (process running for 0.399)
```
Observe the super-fast startup of the Quotes app, a 10x startup time improvement.
Note that the improvement would be even more substantial for real-world applications, due to the larger number of dependencies which can be optimized ahead of time.

__Native Images in Cloud Run__
Native Java images generally benefit only in small measure from enabling CPU Boost. 
From a startup perspective only, they could require as little as a single CPU to be allocated, as startup is a plain container loading effort without any reflection, dynamic proxying, deserialization or other Java specific operations happening.

The running container requires less memory at runtime than a JIT image, due to less classes being loaded and consumes less CPU as well, as no further runtime optimizations are required at runtime.

## __JVM Checkpoint and Restore (CRaC)__
CRaC OpenJDK is an emerging open-source project focused on improving runtime efficiency in Java applications. 
It is based on [CRIU](https://github.com/checkpoint-restore/criu), 
a project that implements checkpoint/restore functionality on Linux, which allows you to run CRaC images only on Linux.

Building a CRaC application follows the same compile process as JIT images do. At runtime, the Java application is started normally, with the optional execution of a number of requests. At this time, a memory snapshot of the running Java application is  triggered (checkpointed), stored to disk in a new image and then restored super-fast when the application is restarted. Thus, CRaC can save a significant amount of startup time, especially for large and complex applications.

As a developer, be aware that file descriptors, sockets and pools are objects in memory, which you have to gracefully close, then restore according to the CRaC lifecycle.

To deploy your CRaC container image to Cloud Run, build the regular Docker image, with a CRaC OpenJDK and start it (see Quotes CRaC sample). Checkpoint the running app and store the resulting container image in Artifact Registry, then deploy it to Cloud Run. You can now update your production application configuration at restoration time in Cloud Run, thus preventing any potential leak of sensitive information, say your production database password. Let’s not forget that everything loaded in memory at startup will be serialized to the snapshot files!!

CRaC OpenJDK is being developed by Azul, which has made CRaC OpenJDK available under an open source license. CRaC OpenJDK is not yet supported by all Java frameworks and libraries. Spring Boot, used for all samples in this blog post, has introduced CRaC support in with the 3.2 GA version.

__Fast build time + less initial optimization → super-fast startup + higher resource consumption__

![Image5.png](images/Image5.png)

__Why use Project CRaC?__

![Image6.png](images/Image6.png)

__Any Project CRaC trade-offs?__

![Image7.png](images/Image7.png)

__Warmup for peak performance with CRaC__

While peak performance of applications checkpointed/restored using CRaC follows the same process as JIT based applications, 
with Hotspot optimization at runtime, instant peak performance is dependent on when the checkpoint has been taken.

Snapshots taken in CI/CD pipeline may only capture web framework and app initialization.
Checkpointing the application after requests have been executed may allow application to reach instant peak performance.

Checkpointing secrets in memory before startup can lead to leaking sensitive data in snapshots as the secret will be serialized in the snapshot.
To mitigate this, checkpoint right after application startup and [refresh the context](https://docs.spring.io/spring-framework/reference/6.1/integration/checkpoint-restore.html#_automatic_checkpointrestore_at_startup).

__Observe__ the [Quotes service](https://github.com/GoogleCloudPlatform/serverless-production-readiness-java-gcp/tree/main/runtimes/crac/quotes-crac/README.md) CRaC Java image checkpoint process:
```shell
❯ ./checkpoint.sh

...
Using CRaC enabled JDK /bin/zulu21.28.89-ca-crac-jdk21.0.0-linux_aarch64.tar.gz
[INFO] Building quotes 1.0.0
...
=> [internal] load metadata for docker.io/library/ubuntu:22.04                                                                              
=> https://.../zulu21.28.89-ca-crac-jdk21.0.0-linux_aarch64.tar.gz                                                        
=> [1/6] FROM docker.io/library/ubuntu:22.04                                                                                             
=> CACHED [2/6] ADD /bin/zulu21.28.89-ca-crac-jdk21.0.0-linux_aarch64.tar.gz
/opt/jdk/openjdk.tar.gz            
...
=> [5/6] COPY target/quotes-crac-1.0.0.jar /opt/app/quotes-crac-1.0.0.jar                                                                
=> [6/6] COPY src/scripts/entrypoint.sh /opt/app/entrypoint.sh                                                                           
=> exporting to image                                                                                                                    
=> => exporting layers                                                                                                                   
=> => writing image
=> => naming to docker.io/library/quotes-crac:builder                                                                                    
...
Please wait during checkpoint creation...
sha256:1e48fde5ae0b39a4a458d659b34e491c9fe965fd757e0b861c61fe891a4796fb
Image: 28982acffecd
```

You can follow the checkpointing process and see the Java app being built, containerized, started then a new image being created during 
checkpointing with a new Docker image digest.

__Note__ the startup time of the [Quotes service](https://github.com/GoogleCloudPlatform/serverless-production-readiness-java-gcp/tree/main/runtimes/crac/quotes-crac/README.md) CRaC Java image restore:
```shell
❯ ./restore.sh
Restarting Spring-managed lifecycle beans after JVM restore
Tomcat started on port 8080 (http) with context path ''
Spring-managed lifecycle restart completed in 13 ms (restored JVM running for 64 ms)
Completed initialization in 2 ms
```

Observe that the startup is super-fast, similar to native images, orders of magnitude faster than JIT images.

__CRaC Images in Cloud Run__

CRaC Native Java images benefit in some measure from enabling CPU Boost in Cloud Run, depending on when the checkpointing has occurred.

If the app has been checkpointed in a CI/CD pipeline, and the application context refreshed at runtime during startup, it would benefit from more CPU during context initialization. In case the app has been checkpointed in a production environment, fully warmed up, CPU boost would not yield any benefits (remember the security limitation above).

At runtime, after the startup phase, CRaC-based apps consume the same amount of CPU and memory as JIT images, as  they are running in a similar manner on a regular JVM.

## A peek into the future of OpenJDK with Project Leyden
I compared two technologies to improve Java application runtime efficiency, each providing significant benefits, but also presenting non-trivial trade-offs. Is there any (future) alternative available, which would allow developers to balance static AOT with dynamic JIT?

[Project Leyden](https://openjdk.org/projects/leyden/) is a new OpenJDK project with the same goal of improving startup/warmup time and 
lowering the footprint of Java applications. The focus of Project Leyden is to allow selectively shifting and constraining computation 
([talk](https://www.youtube.com/watch?v=O1Oz2-AXKKM), [concepts](https://openjdk.org/projects/leyden/notes/03-toward-condensers)), 
while employing the  concept of meaning preservation, i.e. the resulting image has the same meaning as the original, without side effects.

While the project is in its early stages, the Java Platform Group is looking into experimenting with and combining various optimization options. 
One very promising “early” optimization is the combination of [Class Data Sharing](https://docs.oracle.com/en/java/javase/17/vm/class-data-sharing.html#GUID-7EAA3411-8CF0-4D19-BD05-DF5E1780AA91) (CDS) from the JDK 
with [Spring AOT](https://docs.spring.io/spring-framework/reference/core/aot.html), into an [experiment](https://github.com/openjdk/leyden/tree/premain/test/hotspot/jtreg/premain/javac_new_workflow) which shows a 15% startup improvement.

## Additional contributors to runtime efficiency

Runtime efficiency is achieved from faster startup time, smaller container images and lower CPU and memory consumption ([RSS](https://en.wikipedia.org/wiki/Resident_set_size#)).

Observe that Native Java images are smaller than CRaC images, while RSS memory consumption in Native Images is significantly lower than CRaC images (on par with a regular JIT image).

```shelll
❯ docker images | grep quotes
quotes                 latest          231MB
quotes-native          latest          198MB
quotes-crac            checkpoint      994MB

> ps aux | grep quotes
USER     PID     RSS     COMMAND
<user>   47831   541964  java -jar target/quotes-1.0.0.jar
<user>   47901   143848  ./target/quotes
```

## A summary of runtime efficiency optimization options
Let’s summarize all the concepts addressed in this blog post, starting with JIT images and the added optimization technologies available to you when running apps in Cloud Run.

![Image8.png](images/Image8.png)

## Which technology should you use?

Native Java with GraalVM, CRaC and Project Leyden all share  the same goal: improving the runtime efficiency of Java applications running on scale-to-zero platforms, by improving start-up and warm-up time, and reducing resource consumption.

__Native Java with GraalVM__ and __CRaC__ both offer excellent start-up performance, 
typically up to 50x faster than the start-up time on a regular JVM. Peak performance with Java 21 
and profile-guided optimization has both solutions on par.

__Project Leyden__ is a future project that aims to combine the best of both Native Java and CRaC. Leyden will use a new intermediate representation that is designed to be both performant and easy to develop for.

In short:
* __GraalVM technology is production-ready at this time__, with support across all major Java web frameworks with the lowest startup time, resource consumption and security attack surface
* __CRaC is a very good emerging initiative__, with full production readiness dependent on the availability of CRaC lifecycle support in various web frameworks and dependent libraries
* __Project Leyden__ is a future project that has the potential to combine the best of both worlds: static ahead-of-time (AOT) with dynamic just-in-time (JIT)

## Next steps
* Inspect the codebase for the Quotes service illustrated throughout this blog post, 
build both [Quotes Native Java](https://github.com/GoogleCloudPlatform/serverless-production-readiness-java-gcp/blob/main/services/quotes/README.md/README.md) 
or [Quotes CRaC](https://github.com/GoogleCloudPlatform/serverless-production-readiness-java-gcp/tree/main/runtimes/crac/quotes-crac) 
versions and deploy them to Cloud Run
* Peek into the future with OpenJDK’s [Project Leyden](https://github.com/GoogleCloudPlatform/serverless-production-readiness-java-gcp/tree/java21/runtimes/project-leyden)
* For a general overview of Java optimization in Cloud Run, watch the [Developer Stories: Road to Java on GCP Serverless - What can trip you up?](https://cloudonair.withgoogle.com/events/developer-stories-road-to-java-on-gcp-serverless) session

For any questions or feedback, feel free to contact me on Twitter/X [@ddobrin](https://twitter.com/ddobrin).