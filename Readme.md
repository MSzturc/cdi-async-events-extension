cdi-async-events-extension
====================
Summary: An CDI Extension that allows you to send asynchronous CDI Events inside a CDI managed container.

Introduction
---------------------

This project provides a CDI extension for JSR 346: Contexts and Dependency Injection 1.1. This extension enhances the CDI lifecycle by the ability to send asynchronous messages. The project is fully tested and provides several profiles to do easily integration tests in several CDI containers.

This project is build with [Maven 3](http://maven.apache.org/docs/3.0.5/release-notes.html). For integration testing inside diffrent CDI containers [Arquillian](http://arquillian.org/) was used.

Be sure to read this entire document before you attempt to integrate this extension inside your infrastructure. It contains the following information:


* [Overview](#overview): A list of all the features you get out of the box when using this extension

* [System Requirements](#system): List of required to run this extension

* [Build & Install](#build): How to build and install this extension into your local Maven repository.

* [Usage](#run): How to use extension inside your application

* [Technical Notes](#notes): Technical background, used plugins, etc.



<a id="overview"></a>

Overview
---------------------

This project provides a minimalistic single jar CDI extension with a footprint of only 9kb to make your JavaEE6 / JavaEE7 stack ready for asynchronous Events without introducing a JMS provider or similar tools that burst up the complexity of your application. The goal was to create a no brainer extension, so that everyone whos's familiar with CDI Events could put the jar file into a projects classpath annotate the Event which should be consumed asynchronous and the work is done. From the beginning we started to test this extension in several CDI containers which implement several diffrent CDI spec to make sure that this nifty tool could be used in all of the most common application servers. 


<a id="system"></a>

System Requirements
---------------------

The extension is designed to be run on every application server out there that implements CDI 1.0, CDI 1.1 or CDI 1.2 spec. All you need to build this project is Java 8 (Java JDK 1.8) or newer, Maven 3.0 or newer to build the jar and an JavaEE certified application server. If you have not yet installed Maven, see the [Maven Getting Started Guide](http://maven.apache.org/guides/getting-started/index.html) for details. If you plan to use this extension outside a managed environment, in a standalone java-se application please lokk in chapter X for example.

With the prerequisites out of the way, you're ready to build and deploy.


<a id="build"></a>

Build & Install
---------------------
- Clone the cdi-async-events-extension project into a local folder of your filesystem.
- Open a command line and navigate to the root directory of the archetype project
- Use this command to build the archetype:

        mvn clean install

- Check if the archetype is availible in your local maven repository with following command:
        mvn archetype:generate -DarchetypeCatalog=local
  Maven will print a list of all installed archetypes in the local Maven repository. After a successful build you find a  *local -> de.mszturc:cdi-async-events-extension* entry in this list.

- The extension is now build and installed into your local maven repository. To use it inside your maven project add the following dependency into you pom.xml:

        <dependency>
            <groupId>de.mszturc</groupId>
            <artifactId>cdi-async-events-extension</artifactId>
            <version>1.0</version>
            <type>jar</type>
        </dependency>

<a id="run"></a>

Usage
---------------------

We start with following piece of code:

    @ApplicationScoped
    public class CDIEventExample {
    
        @Inject
        Event<LogEvent> sync;
        
        @PostConstruct
        void countDown() {
            for (int idx = 10; idx > 0; idx--) {
                sync.fire(new LogEvent(idx));
            }
        }
    
        public void onLogEvent(@Observes LogEvent event) throws InterruptedException {
            Thread.sleep(1000);
            System.out.println(event.getMessage());
        }
    
    }

After we fire the CDI container up we will console output that counts form 10 down to 1. Between every step the Thread gets blocked for 1 second:

    10
    9
    8
    7
    6
    5
    4
    3
    2
    1
    
Until now nothing new, only old CDI magic. Now lets change the consumer that observes the LogEvent as following:

        public void onLogEvent(@Observes @Async LogEvent event) throws InterruptedException {
            Thread.sleep(1000);
            System.out.println(event.getMessage());
        }
        
This code produces now the following console output:

    10
    7
    8
    9
    6
    3
    4
    5
    1
    2
    
As you can see the order of the output messages is now random. Every number will be printed one second after CDI container fires the ApplicationScope up.


<a id="notes"></a>

Technical Notes
---------------------

For the creation of the project I used the following tools & plugins:

- Java JDK 1.8.0_11-b12 x64
- Maven 3.0.5

The project was test in the following environment:

- Windows 7 x64
- Java JDK 1.8.0_11-b12 x64
- Arquillian 1.1.5
- Maven 3.0.5

On following CDI containers:

- Glassfish 3.1.2.2 ( Weld 1.1.4 ) ( CDI 1.0 )
- Glassfish 4.0.1 ( Weld 2.0.5 ) ( CDI 1.1 )
- Wildfly 8.0 ( 2.1.2 ) ( CDI 1.1 )
- OpenWebBeans ( 1.1.7 ) ( CDI 1.0 )
- Weld-SE ( 2.2.3 ) ( CDI 1.2 )

