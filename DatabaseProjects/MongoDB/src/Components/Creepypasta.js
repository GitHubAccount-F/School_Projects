import React, { Component } from 'react';
import axios from 'axios';
import CreepypastaSub from './CreepypastaSub.js';


export default class Creepypasta extends Component {
    constructor(props) {
        super(props);

        this.state = {creepypastas: []};        
    }

    async componentDidMount() {
        try {
            // Await the axios GET request
            const response = await axios.get('http://localhost:5000/creepypasta/');

            // Update the state with the fetched data
            this.setState({ creepypastas: response.data });
            console.log(response.data);
        } catch (error) {
            console.error('There was an error fetching the data!', error);
        }
    }
    
    createComponents() {
        //console.log(this.state.analogs);
        return this.state.creepypastas.map((creepypasta, index)  => {
            return <CreepypastaSub title={creepypasta.title} pic={creepypasta.pic} key={index}/>;
        })
    }
    

  render() {
    return (
        <div className="container">
            <h1 className="">Page Containing Popular Creepypastas!!!!</h1>
            <table>
                {this.createComponents()}
            </table>
        </div>
    );
  }
};