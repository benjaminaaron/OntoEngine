const path = require("path");
const CopyPlugin = require("copy-webpack-plugin");
const NodePolyfillPlugin = require("node-polyfill-webpack-plugin")

module.exports = {
    entry: "./src/index.js",
    // mode: 'development',
    output: {
        filename: "bundle.js",
        path: path.resolve(__dirname, "../../src/main/resources/static/graph-query-visualizer"),
    },
    // watch: true
    plugins: [
        new CopyPlugin({
            patterns: [
                {
                    from: "./node_modules/@triply/yasgui/build/yasgui.min.css",
                    to: "yasgui.min.css"
                },
            ],
        }),
        new NodePolyfillPlugin() // can this be configured more economically?
    ]
};
