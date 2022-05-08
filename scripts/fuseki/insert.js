const axios = require('axios')

const url = 'http://localhost:3330/update';
const query = 'PREFIX : <http://onto.de/default#> INSERT DATA { GRAPH <http://onto.de/model#main> { :triple :insertedVia :fusekiUpdate } }';

axios({
   method: 'post',
   url: url,
   data: "update=" + query
})
.then(res => console.log('success'))
.catch(error => console.error(error));
