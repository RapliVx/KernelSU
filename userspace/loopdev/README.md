[![Build Status](https://travis-ci.com/mdaffin/loopdev.svg?branch=master)](https://app.travis-ci.com/github/mdaffin/loopdev)
[![crates.io](https://img.shields.io/crates/v/loopdev.svg)](https://crates.io/crates/loopdev)

# loopdev

Setup and control loop devices.

Provides rust interface with similar functionality to the Linux utility `losetup`.

## [Documentation](https://docs.rs/loopdev)

## Examples

```rust
use loopdev::LoopControl;
let lc = LoopControl::open().unwrap();
let ld = lc.next_free().unwrap();

println!("{}", ld.path().unwrap().display());

ld.attach_file("disk.img").unwrap();
// ...
ld.detach().unwrap();
```

## Development

### Running The Tests Locally

Unfortunately the tests require root only syscalls and thus must be run as root.
There is little point in mocking out these syscalls as I want to test they
actually function as expected and if they were to be mocked out then the tests
would not really be testing anything useful.

A vagrant file is provided that can be used to create an environment to safely
run these tests locally as root. With [Vagrant] and [VirtualBox] installed you
can do the following to run the tests.

```bash
vagrant up
vagrant ssh
sudo -i
cd /vagrant
cargo test
```

Note that the tests are built with root privileges, but since vagrant maps this
directory back to the host as your normal user there is minimal issues with
this. At worst the vagrant box will become trashed and can be rebuilt in
minutes.

[vagrant]: https://www.vagrantup.com/docs/installation/
[virtualbox]: https://www.virtualbox.org/
