## Synopsis
This is the expanded, Bluemix version of my demo app, an "App of Ice and Fire". It illustrates the XPages on Bluemix runtime and the flexibility of the XPages runtime in general. For more information, [consult the ReadMe in my master branch of this repository](//github.com/edm00se/AnAppOfIceAndFire/tree/master).

This version of the repository differs in that it contains both the separate data and design ODPs (On Disk Projects), along with the JARs required, added to the design's NSF/JARs path.

## More Info
I should be blogging about this in the near future. Check out my blog for more.

### [edm00se.io](https://edm00se.io/)

## Mock the Domino HTTPServlets (for UI-only editing)
To run without a Domino server (with no back-end logic by the *HTTPServet*s), you can use `json-server`. To do this, you can either install the dependencies specified in the _package.json_ file by running `npm install` or you can install `json-server` globally yourself, via `npm install -g json-server`. [Create a symlink](//www.howtogeek.com/howto/16226/complete-guide-to-symbolic-links-symlinks-on-windows-or-linux/) to the NSF/WebContent path called 'public'; this is how `json-server` will identify the directory with the static assets. To start, from the project path, either run `json-server --id unid --watch housesDB.json --routes routes.json` or just use `npm start`. The _package.json_ file defines the same for the _start_ command that _npm_ will use.

## License
As is my norm for my blog and my GitHub repositories, the work contained herein is licensed under a <a href="//creativecommons.org/licenses/by/3.0/">Creative Commons Attribution 3.0 Unported License</a>. You may use, alter, and redistribute the code herein (with citation), while expecting no warranty for its use.