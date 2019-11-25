# delete fin4kb repo if existent
curl -X DELETE --header 'Accept: */*' 'http://localhost:7200/rest/repositories/fin4kb'
# create new fin4kb repo
curl -X POST http://localhost:7200/rest/repositories -H 'Content-Type: multipart/form-data' -F "config=@repo-config.ttl"

# TODO import fin4.owl

# Using the loadrdf tool would work - requires no other GraphDB instance to run though
# /Applications/GraphDB Free.app/Contents/Java/bin
# ./loadrdf --configFile ~/git/OntoEngine/ontologies/repo-config.ttl --force --mode serial ~/git/OntoEngine/ontologies/fin4.owl
