import React, { Component } from 'react';


export default class CreepypastaSub extends Component {
    constructor(props) {
        super(props);
        this.state = {
                    title: props.title,
                    pic: props.pic, 
                    };
    }

    render() {
        const { title, pic} = this.state;
        return (
            <div>
                <tbody>
                    <tr className='text-center text-white bg-danger pt-5 pb-5 border border-top-3 border-bottom-3'>
                        <th scope="row"><h3>{title}</h3></th>
                        <th scope="row"><img src={pic} alt={title} /></th>
                    </tr>
                </tbody>
            </div>

        )
    }
};