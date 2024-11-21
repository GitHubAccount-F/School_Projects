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

import { LatLngExpression } from "leaflet";
import React, { Component } from "react";
import { MapContainer, TileLayer } from "react-leaflet";
import "leaflet/dist/leaflet.css";
import MapLine from "./MapLine";
import { UW_LATITUDE_CENTER, UW_LONGITUDE_CENTER } from "./Constants";
import {ColoredEdge} from "./types";

// This defines the location of the map. These are the coordinates of the UW Seattle campus
const position: LatLngExpression = [UW_LATITUDE_CENTER, UW_LONGITUDE_CENTER];

interface MapProps {
    //Stores all data involved in creating the lines on the map
  arrOfLines: ColoredEdge[]
}


class Map extends Component<MapProps> {
    constructor(props: any) {
        super(props);
    }
  render() {
        //Creates all lines that are to be displayed on the map,
        // and stores it.
      //Stores all the lines that are to be displayed on the map
      let storage: any[] = []
      if(this.props.arrOfLines.length !== 0) {
          for(let i = 0; i < this.props.arrOfLines.length; i++) {
              storage[i] = <MapLine color={this.props.arrOfLines[i].color}
                                    x1={this.props.arrOfLines[i].x1}
                                    y1={this.props.arrOfLines[i].y1}
                                    x2={this.props.arrOfLines[i].x2}
                                    y2={this.props.arrOfLines[i].y2}
                                    key={this.props.arrOfLines[i].key}
              />
          }
      }
          return (
      <div id="map">
        <MapContainer
          center={position}
          zoom={15}
          scrollWheelZoom={false}
        >
          <TileLayer
            attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
            url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
          />
          {
              storage
          }
        </MapContainer>
      </div>
    );
  }


}

export default Map;
