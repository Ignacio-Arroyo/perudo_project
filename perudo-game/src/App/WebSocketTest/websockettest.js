import React, { useEffect, useState, useCallback } from 'react';
import { Client } from '@stomp/stompjs';

const WebSocketTestPage = () => {
    const [message, setMessage] = useState('');
    const [stompClient, setStompClient] = useState(null);
    const [isConnected, setIsConnected] = useState(false);
    const [connectionStatus, setConnectionStatus] = useState('DISCONNECTED');

    const connectToWebSocket = useCallback(() => {
        const client = new Client({
            // Use WebSocket protocol instead of HTTP
            brokerURL: 'ws://localhost:8080/ws/websocket',
            connectHeaders: {},
            debug: (str) => {
                console.log('STOMP Debug:', str);
            },
            onConnect: () => {
                console.log('Connected to WebSocket');
                setIsConnected(true);
                setConnectionStatus('CONNECTED');
                // Subscribe to a test topic
                client.subscribe('/topic/test', (message) => {
                    console.log('Received message:', message.body);
                });
            },
            onDisconnect: () => {
                console.log('Disconnected from WebSocket');
                setIsConnected(false);
                setConnectionStatus('DISCONNECTED');
            },
            onStompError: (frame) => {
                console.error('STOMP Error:', frame);
                setConnectionStatus('ERROR');
            },
            onWebSocketError: (event) => {
                console.error('WebSocket Error:', event);
                setConnectionStatus('ERROR');
            },
            // Increase reconnect delay to avoid rapid reconnection attempts
            reconnectDelay: 5000,
            // Reduce heartbeat frequency
            heartbeatIncoming: 10000,
            heartbeatOutgoing: 10000,
        });

        client.beforeConnect = () => {
            console.log('Attempting to connect...');
        };

        return client;
    }, []);

    useEffect(() => {
        let client;
        try {
            client = connectToWebSocket();
            setStompClient(client);
            client.activate();
        } catch (error) {
            console.error('Error creating STOMP client:', error);
            setConnectionStatus('ERROR');
        }

        return () => {
            if (client?.active) {
                client.deactivate()
                    .catch(error => console.error('Error deactivating client:', error));
            }
        };
    }, [connectToWebSocket]);

    const handleSendMessage = () => {
        if (stompClient && stompClient.active && isConnected) {
            try {
                stompClient.publish({
                    destination: '/app/sendMessage',
                    body: message,
                });
                setMessage(''); // Clear input after sending
            } catch (error) {
                console.error('Error sending message:', error);
            }
        } else {
            console.error('STOMP client is not connected.');
        }
    };

    return (
        <div>
            <h1>WebSocket Test Page</h1>
            <div style={{ marginBottom: '1rem' }}>
                Status: <span style={{
                    color: connectionStatus === 'CONNECTED' ? 'green' :
                           connectionStatus === 'DISCONNECTED' ? 'orange' : 'red'
                }}>
                    {connectionStatus}
                </span>
            </div>
            <div>
                <input
                    type="text"
                    value={message}
                    onChange={(e) => setMessage(e.target.value)}
                    placeholder="Enter message"
                    disabled={!isConnected}
                />
                <button 
                    onClick={handleSendMessage} 
                    disabled={!isConnected || !message.trim()}
                >
                    Send Message
                </button>
            </div>
        </div>
    );
};

export default WebSocketTestPage;
