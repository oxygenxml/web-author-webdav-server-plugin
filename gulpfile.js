var gulp = require('gulp');
var Synci18n = require('sync-i18n');
var concat = require('gulp-concat');
var uglify = require('gulp-uglify');

var webLocation = 'web';
var targetLocation = "target/web";

gulp.task('i18n', function (done) {
  Synci18n().generateTranslations();
  done();
});


/**
 * Concatenate all js files into one, then uglify it.
 */
gulp.task('prepare-package', gulp.series('i18n', function() {
  return gulp.src(webLocation + '/*.js')
    .pipe(concat('plugin.js'))
    .pipe(uglify())
    .pipe(gulp.dest(targetLocation));
}));

gulp.task('default', gulp.series('prepare-package'));
