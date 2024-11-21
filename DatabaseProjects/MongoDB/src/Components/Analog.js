import React, { Component } from 'react';
import axios from 'axios';
import AnalogSub from './AnalogSub.js';


export default class Analog extends Component {
    constructor(props) {
        super(props);

        this.state = {analogs: []};        
    }

    async componentDidMount() {
        try {
            // Await the axios GET request
            const response = await axios.get('http://localhost:5000/analog/');

            // Update the state with the fetched data
            this.setState({ analogs: response.data });
            console.log(response.data);
        } catch (error) {
            console.error('There was an error fetching the data!', error);
        }
    }
    
    createComponents() {
        //console.log(this.state.analogs);
        return this.state.analogs.map(analog  => {
            console.log(analog);
            return <AnalogSub title={analog.title} pic={analog.pic} link={analog.link} key={analog._id}/>;
        })
    }
    

  render() {
    return (
        <div className="container bg-dark">
            <h1 className="text-white underline">Page Containing Popular Analog Stories</h1>
            <table>
                {this.createComponents()}
            </table>
        </div>
    );
  }
};