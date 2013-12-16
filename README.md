# ajutor - Helper functions and services.

## Features

### DOI RA Proxy

A local DOI registration agency proxy. For a given DOI, returns whether the DOI is a CrossRef DOI, or registered to an other RA, or not a valid DOI. For use where you want to make very heavy queries to check the validity of DOIs. 

Behaves the same as the CrossRef [RA API][1], except for non-crossref RAs it only returns 'Other RA'. Also includes a header "DOI-RA" which returns one of "CrossRef", "Other", "Invalid", "Does not exist" for speed of parsing. This is the suggested method for using this API. 

If there is an error fetching the DOI then a 500 response code will be returned.

CrossRef and Other registered DOIs are permanently cached in MongoDb. Invalid DOIs are cached for 1 day before expiring. The initial MongoDB starter dataset weighs in at around 50,000,000 valid DOIs.

[1]: http://doi.crossref.org/ra/

## Prerequisites

You will need [Leiningen][2] 1.7.0 or above installed.

[2]: https://github.com/technomancy/leiningen

## Running

To start a web server for the application, run:

    lein ring server

## License

Copyright © 2013 CrossRef

## Todo

 - Configure connection strings
 - Reconnect to mongo robustly 