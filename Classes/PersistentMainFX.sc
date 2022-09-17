PersistentMainFX {
  classvar <synth;
  classvar <numChans;
  classvar <enabled=false;
  classvar <synthdefName;
  classvar <>addAfterNode;
  classvar <synthArgs;
  classvar <func;
  classvar <>forceRebuild=false;

  *new {
    ^this.init();
  }

  *rebuild{|...args|
    forceRebuild = true;
    this.init(*args);
  }

  *init {|addAfter=1|
    addAfterNode=addAfter;
    numChans = Server.local.options.numOutputBusChannels;
    synthArgs = ();
    func = this.treeFunc;

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
        if(enabled.not or: { forceRebuild },{
          // This will respawn the synth on hardstop/cmd-period. Inspired by SafetyNet
          ServerTree.add(func, Server.local);
          func.value;
          enabled = true;
          this.afterSynthInit();
          forceRebuild = false; // Only rebuild once
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
    synthdefName = (this.name.asString.toLower ++ numChans).asSymbol;

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
