#[macro_use]
extern crate clap;
extern crate loopdev;

use loopdev::{LoopControl, LoopDevice};
use std::io::{self, Write};
use std::process::exit;

fn find() -> io::Result<()> {
    println!(
        "{}",
        LoopControl::open()?.next_free()?.path().unwrap().display()
    );
    Ok(())
}

fn attach(matches: &clap::ArgMatches) -> io::Result<()> {
    let quiet = matches.is_present("quiet");
    let image = matches.value_of("image").unwrap();
    let offset = value_t!(matches.value_of("offset"), u64).unwrap_or(0);
    let size_limit = value_t!(matches.value_of("sizelimit"), u64).unwrap_or(0);
    let read_only = matches.is_present("read_only");
    let auto_clear = matches.is_present("auto_clear");
    let part_scan = matches.is_present("part_scan");
    let loopdev = match matches.value_of("loopdev") {
        Some(loopdev) => LoopDevice::open(&loopdev)?,
        None => LoopControl::open().and_then(|lc| lc.next_free())?,
    };
    loopdev
        .with()
        .offset(offset)
        .size_limit(size_limit)
        .read_only(read_only)
        .autoclear(auto_clear)
        .part_scan(part_scan)
        .attach(image)?;

    if !quiet {
        println!("{}", loopdev.path().unwrap().display());
    }
    Ok(())
}

fn detach(matches: &clap::ArgMatches) -> io::Result<()> {
    let loopdev = matches.value_of("file").unwrap();
    LoopDevice::open(loopdev)?.detach()
}

fn set_capacity(matches: &clap::ArgMatches) -> io::Result<()> {
    let loopdev = matches.value_of("file").unwrap();
    LoopDevice::open(loopdev)?.set_capacity()
}

fn list(matches: Option<&clap::ArgMatches>) -> io::Result<()> {
    let (_free, _used) = match matches {
        Some(matches) => (matches.is_present("free"), matches.is_present("used")),
        None => (false, false),
    };
    unimplemented!();
}

fn main() {
    let matches = clap_app!(losetup =>
        (version: crate_version!())
        (author: crate_authors!())
        (about: crate_description!())
        (@subcommand find =>
            (about: "find the next free loop device")
        )
        (@subcommand attach =>
            (about: "attach the loop device to a backing file")
            (@arg image: +required "the backing file to attach")
            (@arg loopdev: "the loop device to attach")
            (@arg offset: -o --offset +takes_value "the offset within the file to start at")
            (@arg sizelimit: -s --sizelimit +takes_value "the file is limited to this size")
            (@arg read_only: -r --readonly "set up a read-only loop device")
            (@arg auto_clear: -a --autoclear "set the autoclear flag")
            (@arg part_scan: -p --partscan "set the part-scan flag")
            (@arg quiet: -q --quiet "don't print the device name")
        )
        (@subcommand detach =>
            (about: "detach the loop device from the backing file")
            (@arg file: +required "The file to detach")
        )
        (@subcommand setcapacity =>
            (about: "inform the loop driver of a change in size of the backing file")
            (@arg file: +required "The file to set the capacity of")
        )
        (@subcommand list =>
            (about: "list the available loop devices")
            (@arg free: -f --free "find free devices")
            (@arg used: -u --used "find used devices")
        )
    )
    .get_matches();

    let result = match matches.subcommand() {
        ("find", _) => find(),
        ("attach", Some(matches)) => attach(matches),
        ("detach", Some(matches)) => detach(matches),
        ("setcapacity", Some(matches)) => set_capacity(matches),
        (_, matches) => list(matches),
    };

    if let Err(err) = result {
        writeln!(&mut std::io::stderr(), "{}", err).unwrap();
        exit(1);
    }
}
