# TMDBMovies-2
Second Phase of Movies project, loaded again after erroneous sync last time
1. 10% for Java Code for Http, Database, ContentProvider, FileHandler etc, 90% fighting with the View components! 
For a tiny task of getting a Guideline (replaced witha View now) to be placed below Synopsis TextView AFTER rendering, 
it has taken many days and still, invalidate() throws Exception, hence commented out. It still works through. 
2. Sizes of posters and images to be fetched are computed by a CommonData class based on screen width and resolution, 
instead of creating too many layout files.
3. CommonData class also handles FileI/O instead of using FileProvider, since complex methods are needed.
4. On Setting to favourite, that thumbnail and poster are saved as files with movideId.jpg 
under respective directories (which are private).
5. If selected as favourite, the ID text field is displayed in Yellow, else white.
6. If Offline, favouries only can be viewed just as when online except that Trailers / Reviews will not show and 
Setting / unSetting favourites is discouraged.
7. To test  offline, the boolean value TEST_OFFLINE in MainActivity should be set as true. Set back to false for normal working.
8. Code can be written easily to select the correct image sizes for various Configuration parameters. However, 
sw768 layout has been created for conformity.
9. The first Trailer, if available, can be shared through SMS or WhatsApp or any.
