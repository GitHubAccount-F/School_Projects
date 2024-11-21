

import React, {Component} from 'react';
import Map from "./Map";
import DropDown from "./DropDown";

// Allows us to write CSS styles inside App.css, any styles will apply to all components inside <App />
import "./App.css";
import {CoordinatePairs} from "./types";
import {start} from "repl";

interface AppState {
    //A list of small paths that will be
    //used to create an overall path in the "Map"
    //component.
    arrOfCoor: CoordinatePairs[]
    //Holds the acronym and the real name
    // of all campus building name
    shortToLongNames: [string,string][]
    //Used to store options in the dropdown menu shown in "DropDown"
    longNamesOnly: any[];
}
class App extends Component<{}, AppState> {

    constructor(props: any) {
        super(props);
        this.state = {
            arrOfCoor: [],
            shortToLongNames: [],
            longNamesOnly: [],
        };
    }
    //Initializes our App to retrieve the building information first
    componentDidMount() {
        this.getAllBuildingNames();
    }

    render() {
        return (
            <div>
                <h1 id="app-title">Campus Map!</h1>
                <div>
                    <Map arrOfLines={this.state.arrOfCoor}/>
                </div>
                <DropDown onChange={(value:CoordinatePairs[]) => {this.setState({arrOfCoor: value})}}
                          shortToLong={this.state.shortToLongNames} store={this.state.longNamesOnly}/>
            </div>
        );
    }

    //Retrieves UW campus buildings and stores them inside a
    //list with the acronym of the building's name, and it's proper long name/
    getAllBuildingNames = async () => {
        //
        try {
            let allBuildings = await fetch("http://localhost:4567/buildings");
            // Checks status code to make sure it's OK=200:
            if (!allBuildings.ok) {
                alert("The status is wrong! Expected: 200, Was: " + allBuildings.status)
            }
            //Stores information from localhost:4567 in a form that can be used
            let buildingsString: [string, string][] = Object.entries(await allBuildings.json());
            //creates a list that stores our options in the dropdown menu
            let storage: any[] = [];
            //First option represents that user having not selected any buildings yet
            storage[0] = <option key={0} value={"               "}>{"               "}</option>
            for(let i = 1; i <= buildingsString.length; i++) {
                storage[i] = <option key={i} value={buildingsString[i - 1][1]}>{buildingsString[i - 1][1]}</option>
            }
            this.setState({shortToLongNames:buildingsString});
            this.setState({longNamesOnly: storage});
        } catch (e) {
            alert("There was an error contacting the server.");
            console.log(e);
        }
    }
}

export default App;
