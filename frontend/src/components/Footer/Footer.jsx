import React from 'react';
import "./Footer.css";

const Footer = () => {  
    return (
        <footer className='footer'>
            <div className='top'>
                <div className='branding'>
                <div className='readify-box'>
                    <h2>Readify.</h2>
                    </div>
                    <p>A room without books is like a body without a soul.</p>
                </div>
                <div className='social-links'>
                    <a href="https://www.facebook.com" target="_blank" rel="noopener noreferrer" aria-label="Facebook">
                        <i className='fa-brands fa-facebook-square'></i>
                    </a>
                    <a href="https://www.instagram.com/readify.2025/" target="_blank" rel="noopener noreferrer" aria-label="Instagram">
                        <i className='fa-brands fa-instagram-square'></i>
                    </a>
                    <a href="https://twitter.com" target="_blank" rel="noopener noreferrer" aria-label="Twitter">
                        <i className='fa-brands fa-square-x-twitter'></i>
                    </a>
                </div>
            </div>

            <div className='bottom'>
                <div>
                    <h4>Project</h4>
                    <a href="#">Change log</a>
                    <a href="#">Status</a>
                    <a href="#">License</a>
                    <a href="#">All versions</a>
                </div>
                <div>
                    <h4>Community</h4>
                    <a href="#">Github</a>
                    <a href="#">Issues</a>
                    <a href="#">Project</a>
                    <a href="#">Twitter</a>
                </div>
                <div>
                    <h4>Help</h4>
                    <a href="#">Support</a>
                    <a href="#">Troubleshooting</a>
                    <a href="#">Contact Us</a>
                </div>
                <div>
                    <h4>Others</h4>
                    <a href="#">Terms of Services</a>
                    <a href="#">Privacy Policy</a>
                    <a href="#">License</a>
                </div>
            </div>

            
            <div>
                <h5 classname="Copy"> © 2025 Readify by HCLC (M) SDN BHD (204105036631). All Rights Reserved.  
                </h5>
            </div>
        </footer>
    );
}

export default Footer;
