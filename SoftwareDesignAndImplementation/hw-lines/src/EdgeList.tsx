/*
 * Copyright (C) 2022 Kevin Zatloukal and James Wilcox.  All rights reserved.  Permission is
 * hereby granted to students registered for University of Washington
 * CSE 331 for use solely during Autumn Quarter 2022 for purposes of
 * the course.  No other use, copying, distribution, or modification
 * is permitted without prior written consent. Copyrights for
 * third-party components of this work must be honored.  Instructors
 * interested in reusing these course materials should contact the
 * author.
 */

import React, {Component} from 'react';
import {ColoredEdge} from "./types";

interface EdgeListProps {
    //This method stores data inside a text-box
    //in a form that is easy to retrieve data.
    //This method is used to send information back
    //to the parent component, which is "App" for it to use
    onChange(edges: ColoredEdge[]): void;
}

interface EdgeListState {
    //Holds the text inside the text-box
    allText : string
}

/**
 * A text field that allows the user to enter the list of edges.
 * Also contains the buttons that the user will use to interact with the app.
 */
class EdgeList extends Component<EdgeListProps,EdgeListState> {

    constructor(props: any) {
        super(props);
        this.state = {
            allText: ""
        };
    }
    render() {
        return (
            <div id="edge-list">
                Edges <br/>
                <textarea
                    rows={5}
                    cols={30}
                    onChange={this.handleInputChange}
                    value={this.state.allText}
                /> <br/>
                <button onClick={() => this.props.onChange(this.convertToArr())}>Draw</button>
                <button onClick={() => this.props.onChange(this.eraseEverything())}>Clear</button>
            </div>
        );
    }

    //Used to help update text inside textbook while user is entering/deleting
    //characters
    handleInputChange = (event: any): void => {
        this.setState({allText:event.target.value})
    };

    //parses text inside textbook to deem if it's appropriate to
    //be used on the map. Returns a list of edge data
    //that can be used to create lines on the map.
    convertToArr = (): ColoredEdge[] => {
        if(this.state.allText.length === 0) {
            alert("Please enter a correct input: x1 y1 x2 y2 COLOR");
            return [];
        }
        //splits apart all the lines, storing each
        let trimmedState: string = this.state.allText.trim();
        let stringSplit: string [] = trimmedState.split("\n")
        let index: number = 0;
        //eliminates blank lines
        while (index < stringSplit.length) {
            if (stringSplit[index].length === 0) {
                stringSplit.splice(index, 1);
            } else {
                index++;
            }
        }
        let arrWithEdges: ColoredEdge[] = [];
        //parses the data in each line
        for(let i = 0; i < stringSplit.length; i++) {
            let splitLine = stringSplit[i].split(" ");

            //Eliminates white space in-front of each
            //word, which is better for user experience.
            let index : number = 0;
            while (index < splitLine.length) {
                if (splitLine[index].length === 0) {
                    splitLine.splice(index,1);
                } else {
                    index++;
                }
            }

            //makes sure only five inputs are given on each line
            if (splitLine.length > 5 || splitLine.length < 5) {
                alert("Please include the exact elements and nothing " +
                    "else on each line: x1 y1 x2 y2 COLOR. \nA cause could be blank lines. " +
                    "They are fine, but you " +
                    "cannot enter random characters(such as spaces) inside of it." +
                    " If you did this," +
                    " delete the line or replace it with the " +
                    "the format: x1 y1 x2 y2 COLOR");
                return [];
            }

            //makes sure all coordinates given are purely numbers and between 0 - 4000
            for(let i = 0; i < splitLine.length - 1; i++) {
                if (isNaN(Number(splitLine[i]))) {
                    alert("Please enter only numbers for the coordinates: x1 y1 x2 y2");
                    return [];
                } else if(Number(splitLine[i]) < 0 || Number(splitLine[i]) > 4000) {
                    alert("Please enter only coordinates in the range 0 - 4000 for x1 y1 x2 y2");
                    return [];
                }
            }

            //stores the values given in each line
            let line: ColoredEdge= {
                x1: Number(splitLine[0]),
                y1: Number(splitLine[1]),
                x2: Number(splitLine[2]),
                y2: Number(splitLine[3]),
                color: splitLine[4],
                key: "" + i};
                arrWithEdges[i] = line;
        }
        return arrWithEdges
    }

    //Clears the map of all lines and clears
    //the textbox of all words
    eraseEverything = ():ColoredEdge[] => {
        this.setState({allText: ""})
        return [];
    }

}

export default EdgeList;
