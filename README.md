# CT Renderer

An application to render CT scans and view the data using various techniques, including:
* Normal CT rendering
* Maximum Intensity Projection
* Volume rendering
* Gradient shading
* Smooth gradient shading using interpolation

When volume rendering the opacity of the skin is able to be changed. Also, when using either gradient shading option, the light source is able to be moved along the X axis. Both of these features are controlled using their respective sliders located in the side bar.

## Use your own datasets:
All datasets that use Hounsfield unit are possible to be rendered using this application. In order to render a CT scan of your own you must have the dimensions of the 3D dataset, the dataset must also be headerless. Place your CT scan into the directory named "data", it will then be selectable in the drop down menu to load a CT scan.

## How to compile and run:

In the command line run the following instructions.

### `javac -cp .:/<Path to this directory>/src/ src/Main.java`

### `java -cp .:/<Path to this directory>/src/ Main`

<br/>

