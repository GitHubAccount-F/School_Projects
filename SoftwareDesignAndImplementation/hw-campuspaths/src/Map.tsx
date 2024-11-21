

import { LatLngExpression } from "leaflet";
import React, {ClassicComponent, Component, ReactElement} from "react";
import { MapContainer, TileLayer } from "react-leaflet";
import "leaflet/dist/leaflet.css";
import MapLine from "./MapLine";
import { UW_LATITUDE_CENTER, UW_LONGITUDE_CENTER } from "./Constants";
import {CoordinatePairs} from "./types";

// This defines the location of the map. These are the coordinates of the UW Seattle campus
const position: LatLngExpression = [UW_LATITUDE_CENTER, UW_LONGITUDE_CENTER];

// NOTE: This component is a suggestion for you to use, if you would like to. If
// you don't want to use this component, you're free to delete it or replace it
// with your hw-lines Map

interface MapProps {
    //Holds a list of small paths, where we can use these
    //to create an overall path between two buildings
    arrOfLines: CoordinatePairs[]
}

interface MapState {}

class Map extends Component<MapProps, MapState> {

    constructor(props: any) {
        super(props);
    }
  render() {
        //Creates a list that have MapLine components. These
      //create lines on our map.
      let storage: ReactElement[] = []
      console.log("bye there")
      if(this.props.arrOfLines.length !== 0) {
          console.log("hello there")
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
