import React, {useEffect, useState} from 'react';
import {
  SafeAreaView,
  Text,
  NativeModules,
  Button,
  StyleSheet,
} from 'react-native';
import {
  Pusher,
  PusherMember,
  PusherChannel,
  PusherEvent,
} from '@pusher/pusher-websocket-react-native';

const App = () => {
  // our custom method
  const {ReactOneCustomMethod} = NativeModules;
  module.exports = NativeModules.ReactOneCustomMethod;

  const [id, setId] = useState('Press the button to get The ID');

  useEffect(() => {
    ReactOneCustomMethod.bind();

    initPusher();
  }, []);

  async function initPusher() {
    const pusher = Pusher.getInstance();

    await pusher.init({
      apiKey: 'a562f1e7112d80b3ff1a',
      cluster: 'ap1',
    });

    await pusher.connect();
    await pusher.subscribe({
      channelName: 'my-channel',
      onEvent: event => {
        // console.log(`Event received: ${event}`);

        let pusherData = event?.data;

        getId(pusherData);
        ReactOneCustomMethod.show(`${event?.data}`, ReactOneCustomMethod.SHORT);
      },
    });
  }

  const getId = printerData => {
    console.log('PrinterData', printerData);
    ReactOneCustomMethod.register(true);

    // let data = JSON.stringify(printerData);

    // sale[{ title:'hi',amount:'1000MMK' }}]
    ReactOneCustomMethod.startPrint(printerData);
    // ReactOneCustomMethod.startPrint('KO SOE MOE AUNG');
    // ReactOneCustomMethod.getPhoneID()
    //   .then(res => {
    //     setId('ID: ' + res);
    //   })
    //   .catch(err => {
    //     console.error(err);
    //   });
    // ReactOneCustomMethod.show('Awesome', ReactOneCustomMethod.SHORT);
  };

  return (
    <SafeAreaView style={styles.wrapper}>
      <Text style={styles.id}>{id}</Text>
      <Button title="Get Id" onPress={getId} />
    </SafeAreaView>
  );
};

const styles = StyleSheet.create({
  wrapper: {
    display: 'flex',
    flex: 1,
    justifyContent: 'center',
    paddingHorizontal: 20,
  },
  id: {
    textAlign: 'center',
    marginBottom: 20,
  },
});

export default App;
