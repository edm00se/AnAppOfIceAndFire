## Synopsis

This is the "IBM ConnectED 2015 App-That-Never-Was", as my session became a Chalk Talk.

## Motivation

For all intents and purposes, this _could_ have been the app I was going to demonstrate, with some key exceptions. I am providing this repository as a demonstration of what a "modern, front-end JS heavy, Java servlet (with business logic) based app" can be. For more information, please consult [my blog's series on servlets](//edm00se.io/servlet-series/).

## Installation

Clone the _master_ branch of this repository into a directory of your choice. Then, in DDE's Package Explorer, perform an import of "Existing Project into Workspace", selecting the NSF directory in this repository. Right-click on the project, select "Team Development" then "Associate with New NSF".

## Dependencies

This application, as my blog series describes, requires some JAR files to be contained within the _<Domino install>/jvm/lib/ext/_ path.

* GSON
* several Apache Commons

I've marked where I've used GSON in my Java classes and have almost entirely provided the working _com.ibm.commons.util.io.json_ equivalent. You can also manually work around the Apache Commons libraries, as they're mostly just helper utilities. I do recommend using them though, so if you can't access your server's file system, you can import them into the NSF.

For reasoning on why I recommend placing those JARs on your server, please consult [my blog post on the subject](//edm00se.io/xpages/a-quick-note-on-JARs).

## License

As is my norm for my blog and my GitHub repositories, the work contained herein is licensed under a <a href="//creativecommons.org/licenses/by/3.0/">Creative Commons Attribution 3.0 Unported License</a>. You may use, alter, and redistribute the code herein (with citation), while expecting no warranty for its use.