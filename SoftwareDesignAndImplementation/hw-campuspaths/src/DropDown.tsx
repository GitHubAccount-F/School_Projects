

import React, { Component } from "react";
import {CoordinatePairs, Path} from "./types";
import {start} from "repl";

interface DropDownProps {
    //This method stores data from the dropdowns
    //in a form that is easy to retrieve data.
    //This method is used to send information back
    //to the parent component, which is "App" for it to use
    onChange(value: CoordinatePairs[]): void;

    //Contains the short, abbreviated names of all
    //buildings on campus and their corresponding long
    //names
    shortToLong: [string,string][];

    //Stores all the options for the dropdown menu
    store: any[]
}

interface DropDownState {
    //The current building we are starting at
    start: string;
    //The building we want to arrive at.
    end: string;
}
class DropDown extends Component<DropDownProps, DropDownState> {

    constructor(props: any) {
        super(props);
        this.state = {
            //The first option in the drop down menu has the value "               ",
            //so we use initialize our start/end to it as well. It represents
            //having no values yet.
            start: "               ",
            end: "               "
        };
    }

    render() {

        return (
            <div id="edge-list">
                Enter in the UW campus Buildings where you want to find the
                shortest path between!<br/>
                <br/>
                <select onChange={this.handleInputChange1} value={this.state.start}>
                    {this.props.store}
                </select>
                <span> to </span>
                <select onChange={this.handleInputChange2} value={this.state.end}>
                    {this.props.store}
                </select>
                <br/>
                <button onClick={() => this.analyzeResults()}>Draw</button>
                <button onClick={() => this.props.onChange(this.eraseEverything())}>Clear</button>
            </div>
        );
    }
//
    //Used to help record the starting building that the user has selected.
    handleInputChange1 = (event: any): void => {
        this.setState({start: event.target.value});
    };

    //Used to help record our final destination that the user has selected.
    handleInputChange2 = (event: any): void => {
        this.setState({end: event.target.value})
    };

    //Once the user has clicked the "draw" button,
    //we process the buildings they have chose.
    //If the user didn't select one or both buildings
    //in the dropdown, we return an error message to them.
    //If the users have choosen both buildings(our start and
    //destination), we then return a list of paths to "App" that can be
    //used to create an overall path between the two buildings.
    analyzeResults = (): void => {
        //the if-branch checks for invalid input(when the user hasn't
        //finished selecting all buildings)
        if(this.state.start === "               " || this.state.end === "               ") {
            alert("Please choose two buildings.")
            this.props.onChange([]);
        } else {
            this.findPath(this.state.start, this.state.end);
        }
    }

    //When the user clicks the "clear" button,
    //it errases all current inputs in the dropdown
    //and clears the map.
    eraseEverything = ():CoordinatePairs[] => {
        this.setState({start: "               "});
        this.setState({end: "               "})
        return [];
    }

    //Finds the shortest path between two buildings on the
    //campus. Returns a list of smaller paths to "App" for it
    //it to be used to create an overall path.
    findPath = async (beginning:string, final:string) => {
        try {
            let start: string = "";
            let end: string = "";
            //Finds the corresponding short-names for the
            //buildings selected in the dropdowns.
            for(let i = 0; i < this.props.shortToLong.length; i++) {
                if(this.props.shortToLong[i][1] === beginning) {
                    start = this.props.shortToLong[i][0];
                }
                if(this.props.shortToLong[i][1] === final) {
                    end = this.props.shortToLong[i][0];
                }
            }
            let response: Response = await fetch("http://localhost:4567/path?start="+ start
                + "&end=" + end);
            if (!response.ok) {
                alert("The status is wrong! Expected: 200, Was: " + response.status);
            }
            let parsed: Path = (await response.json()) as Path;
            let store: CoordinatePairs[] = []
            for (let i = 0; i<parsed.path.length; i++) {
                //An array of the starting coordinates for one segment
                // in the format (x,y)
                let startArr: string = Object.values(parsed.path[i])[0];
                //An array of the ending coordinates for one segment
                // in the format (x,y)
                let endArr: string = Object.values(parsed.path[i])[1];
                //These two variables allow us to directly retrieve the information
                let startCoord = Object.values(startArr);
                let endCoord = Object.values(endArr);
                let line: CoordinatePairs = {
                    x1:Number(startCoord[0]), //the x-value in the start
                    y1:Number(startCoord[1]), //the y-value in the start
                    x2:Number(endCoord[0]), //the x-value in the end
                    y2:Number(endCoord[1]), //the y-value in the end
                    color: "black", //color of the whole path
                    key: "" + i
                }
                store[i] = line;
            }
            //this sends our list of small paths back up to "App"
            this.props.onChange(store)
            //fix thsi
        } catch (e) {
            alert("There was an error contacting the server.");
        }
    }
}

export default DropDown;
