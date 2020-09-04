// see http://vuejs-templates.github.io/webpack for documentation.
var path = require('path')
var configPath = ''

if (process.argv.length >= 3) {
  switch (process.argv.splice(2).toString()) {
    case 'dev':
      configPath = './dev.env'
      break
    case 'test':
      configPath = './test.env'
      break
    case 'prod':
    default:
      configPath = './prod.env'
      break
  }
} else {
  configPath = './prod.env'
}
console.log('building for config: ', configPath)

module.exports = {
  build: {
    env: require(configPath),
    index: path.resolve(__dirname, '../dist/index.html'),
    assetsRoot: path.resolve(__dirname, '../dist'),
    assetsSubDirectory: 'static',
    assetsPublicPath: '/',
    productionSourceMap: false,
    // Gzip off by default as many popular static hosts such as
    // Surge or Netlify already gzip all static assets for you.
    // Before setting to `true`, make sure to:
    // npm install --save-dev compression-webpack-plugin
    productionGzip: true,
    productionGzipExtensions: ['js', 'css'],
    // Run the build command with an extra argument to
    // View the bundle analyzer report after build finishes:
    // `npm run build --report`
    // Set to `true` or `false` to always turn it on or off
    bundleAnalyzerReport: process.env.npm_config_report
  },
  dev: {
    env: require('./dev.env'),
    port: 8083,
    autoOpenBrowser: true,
    assetsSubDirectory: 'static',
    assetsPublicPath: '/',
    proxyTable: {
      // '/api': {
      //   target: 'http://40.00.100.100:3002/',// [ext] dev envi. address , http is necessary
      //   changeOrigin: true
      // }
    },
    // CSS Sourcemaps off by default because relative paths are "buggy"
    // with this option, according to the CSS-Loader README
    // (https://github.com/webpack/css-loader#sourcemaps)
    // In our experience, they generally work as expected,
    // just be aware of this issue when enabling this option.
    cssSourceMap: false
  }
}
