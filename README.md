# _SimuLink Viewer Project_
## _Aim and Usage_
The aim of this project is to develop a software tool that can read Simulink MDL files and display their contents in a user-friendly way using a Java-based graphical user interface (GUI). The tool will provide users with the ability to load Simulink MDL files and view their contents in a hierarchical structure. The GUI will allow users to navigate through the model components and see their properties and connections. The software will consist of two main components: a Simulink MDL file parser and a Java-based GUI. The parser will be responsible for reading the MDL file and extracting the model information, including the block diagram, parameters, and connections. The GUI will provide a user-friendly interface for displaying the model.

## _How it works?_
1- We mainly cared about system XML File so we take the MDL files as a long string and look for the XML file we care for to extract it as a new file.

2- We use DOM Parser to extract the data we need about blocks and lines.

3- We made Arrow and Block Super Classes so each time we find a block or a line we can instantiate objects from them and put them in an ArrayList.

4- We used the ArrayLists to draw and view the model in a user-friendly way.

5- We used CSS file for styling.




## _Examples:_
![image](https://github.com/ElecSpartan/AdvProg_SimuLink_Viewer_Project/assets/112751175/88ff54c6-6c29-4f74-9c12-94748f418588)

![image](https://github.com/ElecSpartan/AdvProg_SimuLink_Viewer_Project/assets/112751175/295ca66d-aa1a-415c-bcdb-200046976609)
