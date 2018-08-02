# SeamCarver
The Seam Carver is an assignment from Algorithms, Part II on Coursera. It uses the tological shortest path tree algorithm to search for a vertical and horizontal path of lowest pixel energy values. A pixel energy value is derived from differences in neighbouring pixel rgb values and represents how significant a pixel is in the picture. This concept helps create a content aware picture resizing algorithm that helps address distortions that arise from conventional picture scaling. 

The algorithm for searching shortest path seam used is as follows:

Vertical seam search:

For each pixel, look at the possible three connecting pixels above (less for pixels at edge) and choose pixel with shortest path so far. Path lengths and connecting pixels are kept track in two separate 2d arrays that maps to each pixel. Update the corresponding tracking 2d array element. Perform iteration topologically (ie horizontally first then vertically until all pixels are checked). The shortest path can be found by tracing the pixel with smallest path values in last horizontal layer.

Horizontal seam search:

Repeat of vertical search except applied horizontally. An alternative can be to transpose the pixel 2d array, perform veritcal search, and then transpose back. But this method induces extra calculation costs from 2d array transpose.

An recursive approach using topological path search and vertex relaxation was initially explored. But the algorithm was much slower as it potentially visits each vertex multiple times as opposed to only once in above approach. The reason being that whenever a shorter path is found, all subsequent reachable pixels in the tree downwards will have their paths reevaluated. 
