# PersistentMainFX

This package contains a class that enables all it's subclasses to create a "persistent" effect synth at the main output of SuperCollider. When the user presses command period to stop the sound, a new synth is automatically spawned at the output again. A typical usage for this is adding an output limiter or DC effect to protect the user/speakers.

The class it contains (`PersistentMainFX`) is not used directly but is inherited by a sub class where you implement your synth function.

## Example usage


First, create a class file where you inherit the PersistentMainFX class and implement a synthFunc:
```
// In a class file, eg MainHPF.sc
MainHPF : PersistentMainFX{
  *synthFunc{
    ^{|bus=0, freq=50|

      // input
      var input = In.ar(bus, numChans);

      // Do processing
      var sig = HPF.ar(input, freq);

      ReplaceOut.ar(bus: bus,  channelsArray: sig);
    };

  }
}
```

And then the try out your new high pass filter class in action after a quick recompile and reboot:

```
// Play some sound
play{PinkNoise.ar(0.125)!2};

// Add new hpf effect
MainHPF.new();

// See the effect sitting there:
s.plotTree;

// Try pressing command/ctrl period to stop all sound, and keep an eye on the plot tree and post window (yes, it respawns)
CmdPeriod.run;

// Change some parameters
MainHPF.set(\freq, 950)
```

### Installation

Open up SuperCollider and evaluate the following line of code:
`Quarks.install("https://github.com/madskjeldgaard/persistentmainfx")`
