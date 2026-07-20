import { BrowserRouter as Router, Route, Routes, Link } from 'react-router-dom';
import Main from "./components/Main"
import User from "./components/User"
import Join from "./components/Join"
import Login from "./components/Login"
import Logout from "./components/Logout"
import Predict from "./components/Predict"

function App() {
  return (

    <Router>
      <Link to="/">MAIN</Link> | 
      <Link to="/user">USER</Link> |
      <Link to="/predict">PREDICT</Link> |
      <Link to="/join">JOIN</Link> |
      <Link to="/login">LOGIN</Link> | 
      <Link to="/logout">LOGOUT</Link> | 
      <Routes>
        <Route path="/" element={<Main />} />
        <Route path="/user" element={<User />} />
        <Route path="/predict" element={<Predict />} />
        <Route path="/join" element={<Join />} />
        <Route path="/login" element={<Login />} />
        <Route path="/logout" element={<Logout />} />
      </Routes>
    </Router>
  );
}

export default App;
