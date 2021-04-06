# CT Renderer

An application to render CT scans and view the data using various techniques, including:
* Normal CT rendering
* Maximum Intensity Projection
* Volume rendering
* Gradient shading
* Smooth gradient shading using interpolation

When volume rendering the opacity of the skin is able to be changed. Also, when using either gradient shading option, the light source is able to be moved along the X axis. Both of these features are controlled using their respective sliders located in the side bar.

## Default datasets:
I have included some datasets to be used as default ones. In this case, the file name of the dataset include the dimensions of the X, Y and Z axis in that order. These are the dimensions to be used in the axis length input boxes when loading a file.

e.g  The filename CThead_256x256x113 has an X axis length of 256, a Y axis length of 256 and a Z axis length of 113.

## Visible Human Project
The Visible human project includes a CT scan of a a full human cadaver. I have processed this CT scan for use in this apllication. If you wish to render this, download the scan from the following drop box link : [VHPDataset](https://www.dropbox.com/s/r5sac892nje8ixk/VH_FULL_512_512_1734?dl=0)
**Please note the size of this file tends towards 1GB

Read more about the visible human project, and view the original data here: [Visible Human Project](https://www.nlm.nih.gov/research/visible/visible_human.html)


## Use your own datasets:
All datasets that use Hounsfield unit are possible to be rendered using this application. In order to render a CT scan of your own you must have the dimensions of the 3D dataset, the dataset must also be headerless. Place your CT scan into the directory named "data", it will then be selectable in the drop down menu to load a CT scan. Please note that only binary files are accepted at this point in time.

## How to compile and run:

In the command line run the following instructions.

### `javac -cp .:/<Path to this directory>/src/ src/Main.java`

### `java -cp .:/<Path to this directory>/src/ Main`

<br/>

