import React, {Component} from 'react';
import { withRouter } from 'react-router-dom';
import adminStore from '../../flux/stores/admin';
import {logout} from '../../flux/actions/admin';

import {
  Badge,
  Dropdown,
  DropdownMenu,
  DropdownItem,
  Nav,
  NavItem,
  NavLink,
  NavbarToggler,
  NavbarBrand,
  DropdownToggle
} from 'reactstrap';

class Header extends Component {

  constructor(props) {
    super(props);

    this.goToIndex = this.goToIndex.bind(this);
    this.toggle = this.toggle.bind(this);
    this.state = {
      dropdownOpen: false,
      admin: adminStore.getAdmin()
    };
  }

  componentDidMount() {
    const token = adminStore.getAdmin().token;
    if (!token) {
        this.goToIndex();
        return;
    }
    adminStore.on('adminLoggedOut', this.goToIndex);
  }

  componentWillUnmount() {
    adminStore.removeListener('adminLoggedOut', this.goToIndex);
  }

  goToIndex() {
    return this.props.history.push('/login');
  }

  doLogout() {
     logout();
  }

  toggle() {
    this.setState({
      dropdownOpen: !this.state.dropdownOpen
    });
  }

  sidebarToggle(e) {
    e.preventDefault();
    document.body.classList.toggle('sidebar-hidden');
  }

  sidebarMinimize(e) {
    e.preventDefault();
    document.body.classList.toggle('sidebar-minimized');
  }

  mobileSidebarToggle(e) {
    e.preventDefault();
    document.body.classList.toggle('sidebar-mobile-show');
  }

  asideToggle(e) {
    e.preventDefault();
    document.body.classList.toggle('aside-menu-hidden');
  }

  render() {
    return (
      <header className="app-header navbar">
        <NavbarToggler className="mobile-sidebar-toggler d-lg-none" onClick={this.mobileSidebarToggle}>&#9776;</NavbarToggler>
        <NavbarBrand href="#">SDashboard</NavbarBrand>
        <Nav className="navbar-nav d-md-down-none">
          <NavItem>
            <NavbarToggler className="nav-link sidebar-toggler" type="button"
                           onClick={this.sidebarToggle}>&#9776;</NavbarToggler>
          </NavItem>
          <NavItem className="px-3">
            <NavLink href="#/dashboard">Dashboard</NavLink>
          </NavItem>
          <NavItem className="px-3">
            <NavLink href="#/components/all">Components</NavLink>
          </NavItem>
          <NavItem className="px-3">
            <NavLink href="#/users">Users</NavLink>
          </NavItem>
          <NavItem className="px-3">
            <NavLink href="#/settings">Settings</NavLink>
          </NavItem>
        </Nav>
        <Nav className="navbar-nav ml-auto">
          <NavItem>
            <Dropdown isOpen={this.state.dropdownOpen} toggle={this.toggle}>
              <DropdownToggle className="nav-link dropdown-toggle">
                <span className="d-md-down-none">{adminStore.getAdmin().username}</span>
              </DropdownToggle>
              <DropdownMenu right className={this.state.dropdownOpen ? 'show' : ''}>
                <DropdownItem onClick={this.doLogout}><i className="fa fa-lock"></i> Logout</DropdownItem>
              </DropdownMenu>
            </Dropdown>
          </NavItem>
        </Nav>
      </header>
    )
  }
}

export default withRouter(Header);
