# App of Ice and Fire

> a demo app of how to build a modern web app in a [Domino](http://www.ibm.com/software/products/en/ibmdomino) application container (NSF) with the [XPages](http://xpages.info/) runtime

## Bluemix Edition!
Use these On Disk Projects (ODPs) to build your Data and App NSFs. A live version of this application can be found in its Bluemix form at:
### [iceandfire.mybluemix.net](http://iceandfire.mybluemix.net/)

The front-end portion of the application is treated as being held primarily in the `master` branch. To update the front-end portion of the app in this, the `bluemix` branch:

- commit any changes
- `git checkout bluemix`
- `npm install` \*

\* Running install does the install of development/tooling dependencies, front-end library dependencies, performs the fix (removes `?v=...` from Font-Awesome), and copies the `src/` and `ODP/Code/Java` contents from `master` to their respective destinations. Alternatively, you could run `npm run pull` instead of re-running the install.

This will clear the `src/` directory and pull a copy of the latest committed contents of the `src/` directory from the `master` branch, in addition to the Java classes contained within the `master` branch's `ODP/Code/Java/` path; aka- `master` is where to keep your files. The other scripts here have all been adjusted to work against `App ODP` instead of `ODP`.

## History

This is an evolution of my demo app, "An App of Ice and Fire". There are currently branches for the stand-alone (single NSF) and Bluemix deployable (data and app NSFs segregated), along with the task runners being consolidated and front-end (UI layer of the) app being moved into its source with the `ODP/WebContent/` path being the published, optimized path.

## Dependencies

This application requires some JAR files to be contained within the _<Domino install>/jvm/lib/ext/_ path. Alternatively, they can be added to the application NSF's `Code/JARs` path; the bottom line is that they must reside in the Java build path.

* [com.google.Gson](https://code.google.com/p/google-gson/)
* [Apache Commons IOUtils](http://commons.apache.org/proper/commons-io/)
* a `notes.ini` edit (add _HTTPEnableMethods=PUT,DELETE_ next to your local web preview port) OR adding `PUT` and `DELETE` as allowed methods in your Internet Site document for your server
* the [lwpd.domino.adapter.jar](http://hasselba.ch/blog/?p=746) JAR needs to be added

I've marked where I've used GSON in my Java classes and have almost entirely provided the working _com.ibm.commons.util.io.json_ equivalent. You can also manually work around the Apache Commons libraries, as they're mostly just helper utilities. I do recommend using them though, so if you can't access your server's file system, you can import them into the NSF.

For reasoning on why I recommend placing those JARs on your server, please consult [my blog post on the subject](https://edm00se.io/xpages/a-quick-note-on-JARs).

## Use

Run the commands below from a command line, either from Terminal, etc. on *nix or Git BASH (or BASH environment in 10) for Windows.

| Command            | Result                                    |
| ------------------ | ----------------------------------------- |
| `npm run clean`    | cleans `ODP/WebContent/`                  |
| `npm run build`    | cleans, lints and builds                  |
| `npm run dev`      | preview, lint, build, watch, live reload  |


#### Front-End Development Without a Domino Server (or Designer)
Made possible by `json-server`, you can run `npm run dev` to develop against the front-end assets in `src/` without a Domino server or Domino Designer; this will obviously lack the backing logic by the Domino based *HTTPServet*s.

#### Basic Project Layout
The front-end assets' source is all in `src/`. A build will place the optimized assets into `ODP/WebContent/`. The ODP **must** be imported into your NSF in Domino Designer to run correctly.

```
├── ODP
│   └── WebContent
├── ReadMe.md
├── bower.json
├── db.json
├── gulpfile.js
├── package.json
├── routes.json
└── src
    ├── css
    ├── index.html
    ├── js
    ├── partials
    └── tags
```

## Branches

The `bluemix` branch contains a build optimized into a separate `App ODP` from a `Data ODP`, which correspond to the app and data NSFs, so as to allow for Bluemix deployment. The `master` branch contains a version of things build for single NSF deployment.

## History

The want/need to reconcile the concerns involved in modern front-end tooling combined with Domino/XPages back-end performance is born of a love for the web and automation. For more, read up on my chronicles on [edm00se.io](https://edm00se.io).

## Credits

* [gulp](http://gulpjs.com/)
* [json-server](https://github.com/typicode/json-server)
* [egghead.io's video lesson on using json-server](https://egghead.io/lessons/nodejs-creating-demo-apis-with-json-server)


## License

[The MIT License (MIT)](https://github.com/edm00se/AnAppOfIceAndFire/blob/master/LICENSE.md) © 2016 Eric McCormick
