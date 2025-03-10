import React from 'react';
import './footer.css'; // Optional: Add some basic styling

const Footer = () => {
  return (
    <footer className="footer">
      <div className="footer-content">
        <p>&copy; {new Date().getFullYear()} Perudo Boardgames Club. All rights reserved.</p>
        <div className="footer-links">
          <a href="https://www.facebook.com" target="_blank" rel="noopener noreferrer">Facebook</a>
          <a href="https://www.twitter.com" target="_blank" rel="noopener noreferrer">Twitter</a>
          <a href="https://www.instagram.com" target="_blank" rel="noopener noreferrer">Instagram</a>
        </div>
      </div>
    </footer>
  );
};

export default Footer;
