package com.example.simulinkviewer;

public class Main extends Application {
    static private List<Block> blocks = new ArrayList<Block>(); // for the blocks


public static void addBlocks(Element rootElement, Document doc) {
        if (rootElement.getTagName().equals("System")) {
            NodeList blockList = doc.getElementsByTagName("Block");
            for (int i = 0; i < blockList.getLength(); i++) {
                boolean inputs_ports_position_flag = false; // will be used to check if the block had a input ports number or not
                boolean blockMirror = false; // will be used to check if the block input and output will be mirrored or not
                Node blockNode = blockList.item(i);
                NodeList childNodes = blockNode.getChildNodes();
                if (blockNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element blockElement = (Element) blockNode;

                    //Extracting the information from the block tag
                    String Name = blockElement.getAttribute("Name");
                    String BlockType = blockElement.getAttribute("BlockType");
                    int ID = (Integer.parseInt(blockElement.getAttribute("SID")));

                    //this part parse the position string to extract the 4 coordinates of the block and handles weather it's on index 0 or index 1
                    double value1 = 0, value2 = 2, value3 = 0, value4 = 0;
                    for (int j = 0; j < blockElement.getElementsByTagName("P").getLength(); j++) {
                        if (blockElement.getElementsByTagName("P").item(j).getAttributes().item(0).getTextContent().equals("Position")) {
                            String Position = blockElement.getElementsByTagName("P").item(j).getTextContent();
                            Position = Position.replace("[", "").replace("]", ""); // Remove square brackets
                            String[] strValues = Position.split(","); // Split by comma
                            value1 = Double.parseDouble(strValues[0]);
                            value2 = Double.parseDouble(strValues[1]);
                            value3 = Double.parseDouble(strValues[2]);
                            value4 = Double.parseDouble(strValues[3]);
                        }
                    }


                    // this part extract the number of input and output ports and the flag is used to
                    int NumInputPorts = 1, NumOutputPorts = 1;

                        String ports = blockElement.getElementsByTagName("P").item(0).getTextContent();
                        if (ports.length() == 6) {
                            NumInputPorts = ports.charAt(1) - '0';
                            NumOutputPorts = ports.charAt(4) - '0';
                        } else if (ports.length() == 3) {
                            NumInputPorts = ports.charAt(1) - '0';
                            NumOutputPorts = 0;
                        }


                    // extracting the inputs if the BlockType is ADD
                     String inputs = ""; // for Add class ( signs )
                    if(BlockType.equals("Sum")){
                            for (int j = 0; j < blockElement.getElementsByTagName("P").getLength(); j++) {
                                if (blockElement.getElementsByTagName("P").item(j).getAttributes().item(0).getTextContent().equals("Inputs")) {
                                    inputs = blockElement.getElementsByTagName("P").item(j).getTextContent();
                                    break;
                                }
                            }
                        }

                    for (int j = 0; j < blockElement.getElementsByTagName("P").getLength(); j++) {
                        if (blockElement.getElementsByTagName("P").item(j).getAttributes().item(0).getTextContent().equals("BlockMirror")) {
                            if (blockElement.getElementsByTagName("P").item(j).getTextContent().equals("on"))
                                blockMirror = true;
                        }
                    }

                    String value = "1"; // for constant class ( value )

                    Block b = switch (BlockType) {
                        case "Saturate" ->
                                new Saturation(ID, Name, value1, value2, value3, value4, NumInputPorts, NumOutputPorts, blockMirror);
                        case "UnitDelay" ->
                                new UnitDelay(ID, Name, value1, value2, value3, value4, NumInputPorts, NumOutputPorts, blockMirror);
                        case "Scope" ->
                                new Scope(ID, Name, value1, value2, value3, value4, NumInputPorts, NumOutputPorts, blockMirror);
                        case "Sum" ->
                                new Add(ID, Name, value1, value2, value3, value4, NumInputPorts, NumOutputPorts, blockMirror, inputs);
                        case "Constant" ->
                                new Constant(ID, Name, value1, value2, value3, value4, NumInputPorts, NumOutputPorts, blockMirror, value);
                        default ->
                                new Block(ID, Name, value1, value2, value3, value4, NumInputPorts, NumOutputPorts, blockMirror);
                    };

                    blocks.add(b);
                }
            }
        }
    }
}
