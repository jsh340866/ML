import { useEffect, useState } from "react"
import axios from "axios"
import api from "../api/axiosConfig"
const Join = () => {
    const [username, setUsername] = useState("")
    const [password, setPassword] = useState("")

    const handleJoin = () => {
        // axios.post('http://localhost:8080/join',
        api.post('/join',
            { username, password },
            { headers: { 'Content-Type': 'application/json' } } )
            .then(resp => console.log(resp))
            .catch(err => console.log(err));
    }
    return (

        <div>

            <h1>JOIN PAGE</h1>
            USERNAME : <input type="text" name="username" onChange={e => { setUsername(e.target.value) }} /><br />
            PASSWORD : <input type="password" name="password" onChange={e => { setPassword(e.target.value) }} /><br />
            <button onClick={handleJoin}>JOIN</button>


        </div>
    )
}

export default Join;