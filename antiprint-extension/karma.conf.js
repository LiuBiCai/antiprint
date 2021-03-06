// Karma configuration
// Generated on Wed Oct 25 2017 14:41:03 GMT-0400 (EDT)

module.exports = function(config) {
  config.set({

    // base path that will be used to resolve all patterns (eg. files, exclude)
    basePath: '',


    // frameworks to use
    // available frameworks: https://npmjs.org/browse/keyword/karma-adapter
    frameworks: ['jasmine'],


    // list of files / patterns to load in the browser
    files: [
      'src/main/extension/lib/ua-parser.js',
      'src/main/extension/projection.js',
      require.resolve('karma-fixture-loader'),
      {
        pattern: 'src/test/resources/fixtures/*.json',
        watched: true,
        served: true,
        included: false
      },
      'src/test/extension/*-test.js'
    ],


    // list of files to exclude
    exclude: [
    ],


    // preprocess matching files before serving them to the browser
    // available preprocessors: https://npmjs.org/browse/keyword/karma-preprocessor
    preprocessors: {
    },


    // test results reporter to use
    // possible values: 'dots', 'progress'
    // available reporters: https://npmjs.org/browse/keyword/karma-reporter
    reporters: ['progress'],


    // web server port
    port: 9876,


    // enable / disable colors in the output (reporters and logs)
    colors: true,


    // level of logging
    // possible values: config.LOG_DISABLE || config.LOG_ERROR || config.LOG_WARN || config.LOG_INFO || config.LOG_DEBUG
    logLevel: config.LOG_INFO,


    // enable / disable watching file and executing tests whenever any file changes
    autoWatch: true,


    // start these browsers
    // available browser launchers: https://npmjs.org/browse/keyword/karma-launcher
    browsers: [
        'ChromiumHeadless',
    ],

    customLaunchers: {
        ChromiumHeadlessDockerable: {
          base: 'ChromiumHeadless',
          flags: ['--disable-gpu', '--no-sandbox']
        }
    },

    plugins: [
        'karma-jasmine',
        'karma-chrome-launcher',
    ],

    singleRun: false, // this is overridden (as true) when launched by Maven

    // Concurrency level
    // how many browser should be started simultaneous
    concurrency: 1
  })
};
