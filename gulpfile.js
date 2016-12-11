/* File: gulpfile.js */

// grab our packages
var gulp        = require('gulp'),
  gutil         = require('gulp-util'),
  jshint        = require('gulp-jshint'),
  concat        = require('gulp-concat'),
  sourcemaps    = require('gulp-sourcemaps'),
  minify        = require('gulp-minify-css'),
  rename        = require('gulp-rename'),
  minifyHTML    = require('gulp-minify-html'),
  inject        = require('gulp-inject'),
  del           = require('del'),
  runSequence   = require('run-sequence'),
  uglify        = require('gulp-uglify'),
  ngAnnotate    = require('gulp-ng-annotate'),
  dist          = 'ODP/WebContent';

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
    .pipe(uglify())
    .pipe(sourcemaps.write())
    .pipe(gulp.dest('./'+dist));
});

gulp.task('cssmin', function(){
  gulp.src(['./src/css/*.css'])
    .pipe(minify({ keepBreaks: false }))
    .pipe(concat('style.min.css')) // combines into single minified CSS file
    .pipe(gulp.dest(dist));
});

gulp.task('minify-html-partials', function(){
  var opts = {
    conditionals: true,
    spare: true
  };

  return gulp.src('./src/partials/*.html')
    .pipe(minifyHTML(opts))
    .pipe(gulp.dest('./'+dist+'/partials'));
});

gulp.task('index', function(){
  var target = gulp.src('./src/index.html');
  var sources = gulp.src([
                  './'+dist+'/*.css',
                  './'+dist+'/*.js'
                ], { ignorePath: dist, read: false, relative: true });
  return target.pipe(inject(sources, { ignorePath: dist, addRootSlash: false }))
    .pipe(gulp.dest('./'+dist+''));
});

gulp.task('copyTags', function(){
  gulp.src(['./src/tags/abilities.json'])
    .pipe(gulp.dest('./'+dist+'/tags'));
});

gulp.task('clean', function () {
  return del([
    './'+dist+'/index.html',
    './'+dist+'/partials',
    './'+dist+'/scripts.js',
    './'+dist+'/style.min.css',
    './'+dist+'/tags/abilities.json',
    // we really don't want to clean this dir
    '!./'+dist+'/WEB-INF'
  ]);
});

// configure which files to watch and what tasks to use on file changes
gulp.task('watch', function() {
  gulp.watch('./src/js/*.js', ['build']);
  gulp.watch('./src/css/*.css', ['build']);
  gulp.watch('./src/partials/*.html', ['build'])
});

// main build task
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

// define the default task and add the watch task to it
gulp.task('default', ['build', 'watch']);
