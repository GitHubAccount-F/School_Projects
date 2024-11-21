import React, { Component } from 'react';


export default class AnalogSub extends Component {
    constructor(props) {
        super(props);
        this.state = {
                    title: props.title,
                    pic: props.pic, 
                    link: props.link
                    };
    }

    render() {
        const { title, pic, link } = this.state;
        return (
            <div>
                <tbody>
                    <tr className='text-center text-white'>
                        <th scope="row"><h3>{title}</h3></th>
                        <th scope="row"><img src={pic} alt={title} /></th>
                        <th scope="row"><p><a href={link}>Youtube Link</a></p></th>
                    </tr>
                </tbody>
            </div>

        )
    }

};