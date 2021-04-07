# CT Scan Renderer

An application to render computerized tomography (CT or CAT) scans and view the data using various techniques, including:
* Normal CT rendering
* Slice by slice viewing
* Maximum Intensity Projection
* Volume rendering
* Gradient shading
* Smooth gradient shading using interpolation

When volume rendering the opacity of the skin can be changed. Also, when using either gradient shading option, the light source can be moved along the X-axis. Both of these features are controlled using their respective sliders located in the sidebar.

## Default dataset:
I have included some datasets to be used as default ones. In this case, the file name of the dataset includes the dimensions of the X, Y and Z axis in that order. These are the dimensions to be used in the axis length input boxes when loading a file.

e.g. The filename CThead_256x256x113 has an X-axis length of 256, a Y-axis length of 256 and a Z-axis length of 113.

![defaultdataset](https://user-images.githubusercontent.com/65715894/113886503-a270a500-97b8-11eb-9591-7ba6e3ad0f31.gif)

The original, pre-processed data is from: [Stanford volume archive](https://graphics.stanford.edu/data/voldata/)

## Visible Human Project
The Visible human project includes a CT scan of a full human cadaver. I have processed this CT scan for use in this application. <br>If you wish to render this, download the scan from the following dropbox link: [VHPDataset](https://www.dropbox.com/s/r5sac892nje8ixk/VH_FULL_512_512_1734?dl=0)
* Place this file within the directory 'src/data'.
* Ensure the "big-endian?" and "visible human re-sampling?" checkboxes are checked when loading this file.

To only view the data chest up and increase render speed, set the Z-axis length to 512.<br>
**Please note the size of this file tends towards 1GB**

![VHP](https://user-images.githubusercontent.com/65715894/113885262-9df7bc80-97b7-11eb-9ad2-5d44891ff8f7.gif)

Read more about the visible human project, and view the original data here: [Visible Human Project](https://www.nlm.nih.gov/research/visible/visible_human.html)

## Use your own datasets:
All datasets that use the Hounsfield unit are possible to be rendered using this application. In order to render a CT scan of your own, you must have the dimensions of the 3D dataset, the dataset must also be headerless. Place your CT scan into the directory named "data", it will then be selectable in the drop-down menu to load a CT scan.

Please note that only binary files are accepted at this point in time and that little-endian is assumed unless big-endian is selected.

## How to compile and run:
In the command line run the following instructions.

### `javac -cp .:/<Path to this directory>/src/ src/Main.java`

### `java -cp .:/<Path to this directory>/src/ Main`


<br>

