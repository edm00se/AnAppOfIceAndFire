/* File: gulpfile.js */

// grab our packages
var gulp        = require('gulp'),
  gutil         = require('gulp-util'),
  jshint        = require('gulp-jshint'),
  concat        = require('gulp-concat'),
  sourcemaps    = require('gulp-sourcemaps'),
  jsonServer    = require('gulp-json-srv'),
  minify        = require('gulp-minify-css'),
  rename        = require('gulp-rename'),
  minifyHTML    = require('gulp-minify-html'),
  inject        = require('gulp-inject'),
  del           = require('del'),
  runSequence   = require('run-sequence'),
  uglify        = require('gulp-uglify'),
  ngAnnotate    = require('gulp-ng-annotate'),
  spawn         = require('child_process').spawn,
  server        = jsonServer.start({
          // config the json-server instance
          data: 'db.json',
          id: 'unid',
          rewriteRules: {
            "/xsp/houses": "/houses",
            "/xsp/:houses/:id": "/:houses/:id",
            "/xsp/characters": "/characters",
            "/xsp/:characters/:id": "/:characters/:id"
          },
          deferredStart: true
        }),
  compress      = require('compression'),
  browserSync   = require('browser-sync').create();

// configure the jshint task
gulp.task('jshint', function() {
  return gulp.src(['./gulpfile.js','./src/js/*.js'])
    .pipe(jshint({
      // ideally I'll just clean this up, but leaving here for demonstrative purposes
      '-W033': true, // mising semicolon
      '-W041': true, // use 'x' to compare with 'y'
      '-W004': true, // x already in use
      '-W014': true // bad line breaking before '||'
    }))
    .pipe(jshint.reporter('jshint-stylish'));
});

// build dist JS assets
gulp.task('build-js', function() {
  return gulp.src('./src/js/*.js')
    .pipe(ngAnnotate())
    .pipe(sourcemaps.init())
    .pipe(concat('scripts.js'))
    //only uglify if gulp is ran with '--type production'
    //.pipe(gutil.env.type === 'production' ? uglify() : gutil.noop())
    .pipe(uglify())
    //.on('error', notify.onError("Error: <%= error.message %>"))
    .pipe(sourcemaps.write())
    .pipe(gulp.dest('./public'));
});

gulp.task('cssmin', function(){
  gulp.src(['./src/css/*.css'])
    .pipe(minify({ keepBreaks: false }))
    .pipe(concat('style.min.css')) // combines into single minified CSS file
    .pipe(gulp.dest('public'));
});

gulp.task('minify-html-partials', function(){
  var opts = {
    conditionals: true,
    spare: true
  };

  return gulp.src('./src/partials/*.html')
    .pipe(minifyHTML(opts))
    .pipe(gulp.dest('./public/partials'));
});

gulp.task('index', function(){
  var target = gulp.src('./src/index.html');
  var sources = gulp.src([
                  './public/*.css',
                  './public/*.js'
                ], { ignorePath: 'public', read: false });
  return target.pipe(inject(sources, { ignorePath: 'public' }))
    .pipe(gulp.dest('./public'));
});

gulp.task('copyTags', function(){
  gulp.src(['./src/tags/abilities.json'])
    .pipe(gulp.dest('./public/tags'));
});

gulp.task('clean', function () {
  return del([
    './public/index.html',
    './public/partials',
    './public/scripts.js',
    './public/style.min.css',
    './public/tags/abilities.json',
    // we don't want to clean this file though so we negate the pattern
    '!./public/WEB-INF'
  ]);
});

// configure which files to watch and what tasks to use on file changes
gulp.task('watch', function() {
  gulp.watch('./gulpfile.js', ['auto-reload']);
  gulp.watch('./src/js/*.js', ['build','browser-sync-reload']);
  gulp.watch(['./db.json'], function(){ server.reload(); });
  gulp.watch('./src/css/*.css', ['build','browser-sync-reload']);
  gulp.watch('./src/partials/*.html', ['build','browser-sync-reload'])
});

// starts the json-server instance
gulp.task('serverStart', function(){ server.start(); });

// reload the json-server instance, and its assets
gulp.task('serverReload', function(){ server.reload(); });

// loading browser-sync as a proxy, must load after json-server
gulp.task('browser-sync', function() {
    browserSync.init({
        proxy: {
          target: "http://localhost:3000/",
          middleware: [compress()]
        },
        ui: {
          weinre: {
              port: 9090
          }
      }
    });
});

// reload browserSync
gulp.task('browser-sync-reload', function(){
  browserSync.reload();
});

// generic build, assuming we don't want the preview
gulp.task('build', function(){
  runSequence(
    'clean',
    'minify-html-partials',
    'cssmin',
    'copyTags',
    'jshint',
    'build-js',
    'index');
});

gulp.task('auto-reload', function(){
  spawn('gulp', [], {stdio: 'inherit'});
  process.exit();
});

// define the default task and add the watch task to it
gulp.task('default', ['build', 'watch', 'serverStart','browser-sync']);
