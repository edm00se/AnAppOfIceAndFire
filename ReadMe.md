# Note
You should either dowload the [latest release](https://github.com/edm00se/AnAppOfIceAndFire/releases/latest) or cone from the [corresponding commit](https://github.com/edm00se/AnAppOfIceAndFire/tree/0.5.3.1). The next release will contain all the goodies I'm currently refactoring in; so if you want to use the _master_ branch in its current state, you can, but you need to perform the below steps for setting up your Domino (and Designer) environment (`java.pol`, JARs), followed by the creation of the `public/` symlink (instructions below), then an `npm install` and finally a `bower install` (node required, tested with v4 LTS).

refs: [#5](https://github.com/edm00se/AnAppOfIceAndFire/issues/5), [#6](https://github.com/edm00se/AnAppOfIceAndFire/issues/6)

## Synopsis

This is an evolution of my demo app, "An App of Ice and Fire". There are currently branches for the stand-alone (single NSF) and Bluemix deployable (data and app NSFs segregated), along with the task runners being consolidated and front-end (UI layer of the) app being moved into its source with the `ODP/WebContent/` path being the published, optimized path.

## Dependencies

This application, as my blog series describes, requires some JAR files to be contained within the _<Domino install>/jvm/lib/ext/_ path. Alternatively, they can be added to the application NSF's `Code/JARs` path; the bottom line is that they must reside in the Java build path.

* [com.google.Gson](https://code.google.com/p/google-gson/)
* [Apache Commons IOUtils](http://commons.apache.org/proper/commons-io/)
* a notes.ini edit (add _HTTPEnableMethods=PUT,DELETE_ next to your local web preview port) OR adding PUT and DELETE as allowed methods in your Internet Site document for your server
* the [lwpd.domino.adapter.jar](http://hasselba.ch/blog/?p=746) JAR needs to be added

I've marked where I've used GSON in my Java classes and have almost entirely provided the working _com.ibm.commons.util.io.json_ equivalent. You can also manually work around the Apache Commons libraries, as they're mostly just helper utilities. I do recommend using them though, so if you can't access your server's file system, you can import them into the NSF.

For reasoning on why I recommend placing those JARs on your server, please consult [my blog post on the subject](//edm00se.io/xpages/a-quick-note-on-JARs).

## Run Without Domino Server
To run without a Domino server (with no back-end logic by the *HTTPServet*s), you can use `json-server`. To do this, you can either install the dependencies specified in the _package.json_ file by running `npm install` or you can install `json-server` globally yourself, via `npm install -g json-server`. [Create a symlink](http://www.howtogeek.com/howto/16226/complete-guide-to-symbolic-links-symlinks-on-windows-or-linux/) to the NSF/WebContent path called 'public'; this is how `json-server` will identify the directory with the static assets. To start, from the project path, either run `json-server --id unid --watch housesDB.json --routes routes.json` or just use `npm start`. The _package.json_ file defines the same for the _start_ command that _npm_ will use.

### Task Runner Installation

You must have:

* [git](http://git-scm.com/)
* a current version of [Node](https://nodejs.org/en/) ~~or [io.js](https://iojs.org/en/)~~ (with npm package manager) *note: [io.js merged with Node](http://www.linuxfoundation.org/news-media/announcements/2015/06/nodejs-foundation-advances-community-collaboration-announces-new) again (ca. June 2015), so probably skip io.js
* Internet access

First, clone this repository, then run `npm install` which will install some npm dependencies (including `json-server`, then run `bower install`, which will install the front-end libraries needed. Lastly, you need to symlink a `public/` path to the `ODP/WebContent/` directory.

* for *nix and Mac operating systems, the command is `ln -s ODP/WebContent/ public`
* for Windows, you'll need to start up the command prompt (from the Start/search, "cmd", right-click and select "run as administrator")
    * change directory to the root of the working git repository we set up, then run `mklink /d public ODP\WebContent`
* don't worry about duplicate data, these are both methods for a symbolic link, meaning it's the same file, with multiple path pointers (and the `.gitignore` file is set up to ignore the public path, so we won't pollute our repository with duplicates)

### Usage

Read up on [the blog series on task runners on Domino on edm00se.io](https://edm00se.io/task-runners-with-domino-apps) or try running `npm start` for the original front-end application with back-end mock.

You can check out the other task available via Grunt or gulp by running `grunt` or `gulp`, respectively.

#### Basic Project Layout
The layout has the On Disk Project (ODP, freshly renamed to that in place of a directory called 'NSF', to eliminate confusion) and its respective WebContent/ directory inside of it, containing the production-ready (aka- 'dist', distribution, or built version of the source client-side assets), additionally a 'src' folder at the root to contain the source client-side assets, unmodified, with 'public' pointing at the `ODP/WebContent/` path to provide the built results as the preview in the local browser, in conjunction with `json-server` as implmemented in the `npm start` script or, ideally, the `gulp` tasks.

```
├── Gruntfile.js
├── ODP
│   └── WebContent
├── ReadMe.md
├── bower.json
├── db.json
├── gulpfile.js
├── package.json
├── public -> NSF/WebContent/
├── routes.json
└── src
    ├── css
    ├── index.html
    ├── js
    ├── libs
    ├── partials
    └── tags
```

## History

The want/need to reconcile the concerns involved in modern front-end tooling combined with Domino/XPages back-end performance is born of a love for the web and automation. For more, read up on my chronicles on [edm00se.io](https://edm00se.io).

## Credits

Considerable credit should go to:

* [Grunt](http://gruntjs.com/)
* [gulp](http://gulpjs.com/)
* [json-server](https://github.com/typicode/json-server)
* [egghead.io's video lesson on using json-server](https://egghead.io/lessons/nodejs-creating-demo-apis-with-json-server)
* [scotch.io](https://scotch.io) for having great tutorials on getting started with Grunt and gulp

## License

As is my norm for my blog and my GitHub repositories, the work contained herein is licensed under <a href="http://choosealicense.com/licenses/mit">the MIT License (MIT)</a>. You may use, alter, and redistribute the code herein (with citation), while expecting no warranty for its use.
