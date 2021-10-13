PersistentMainFX {
  classvar <synth;
  classvar <numChans;
  classvar <enabled=false;
  classvar <synthdefName;
  classvar <>addAfterNode;
  classvar <synthArgs;

  *new {
    ^this.init();
  }

  *init {|addAfter=1|
    addAfterNode=addAfter;
    numChans = Server.local.options.numOutputBusChannels;
    synthArgs = ();

    if(Server.local.hasBooted.not, {
      "%: Server is not booted. Won't do anything until it has booted".format(this.name).warn
    });

    if(this.synthFunc.isKindOf(Function).not, { "% synthFunc does not return a function, instead it returns: %!".format(this.name, this.synthFunc.class.name).error});

    Server.local.doWhenBooted{
      fork{

        Server.local.sync;

        this.prepareResources();

        Server.local.sync;
        this.addSynthDef();

        Server.local.sync;
        if(enabled.not,{
          // This will respawn the synth on hardstop/cmd-period. Inspired by SafetyNet
          ServerTree.add(this.treeFunc, Server.local);
          this.treeFunc.value;
          enabled = true;
          this.afterSynthInit();
        }, { "PersistentMainFX % already setup and enabled!".format(this.name).warn});
      }
    }
  }

  // Use this to load buffers or prepare other resources before adding synths
  *prepareResources{}

  *afterSynthInit{}

  *treeFunc{
    ^{
      this.addMessage();

      forkIfNeeded{
        Server.local.sync;

        synth = Synth.after(addAfterNode, synthdefName, synthArgs.asArgsArray ? []);
      }

    }

  }

  *addMessage{
    "Adding % to main output".format(this.name).postln;
  }

  *set{|...args|

    // Store arguments in dict so that they are reused when the synth is respawned after cmd-period
    args.asDict.keysValuesDo{|inKey, inVal|
      synthArgs.put(inKey, inVal)
    };

    synth.set(*args)
  }

  *map{|...args|
    synth.map(*args)
  }

  *addSynthDef{
    synthdefName = (this.name.toLower.asString ++ numChans).asSymbol;

    SynthDef.new(synthdefName, this.synthFunc()).add;

  }

  *synthFunc{
    ^{|bus=0|

      // input
      var input = In.ar(bus, numChans);

      // Do processing
      var sig = input;

      ReplaceOut.ar(bus: bus,  channelsArray: sig);
    }
  }

}

/*
// Example: A High pass filter output
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
*/
